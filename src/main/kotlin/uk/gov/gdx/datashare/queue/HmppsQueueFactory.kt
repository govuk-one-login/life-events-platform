package uk.gov.gdx.datashare.queue

import com.amazon.sqs.javamessaging.ProviderConfiguration
import com.amazon.sqs.javamessaging.SQSConnectionFactory
import com.amazonaws.services.sns.model.SubscribeRequest
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.CreateQueueRequest
import com.amazonaws.services.sqs.model.QueueAttributeName
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.jms.config.DefaultJmsListenerContainerFactory
import javax.jms.Session

class HmppsQueueFactory(
  private val context: ConfigurableApplicationContext,
  private val amazonSqsFactory: AmazonSqsFactory,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun createHmppsQueues(hmppsSqsProperties: HmppsSqsProperties, hmppsTopics: List<HmppsTopic> = listOf()) =
    hmppsSqsProperties.queues
      .map { (queueId, queueConfig) ->
        val sqsDlqClient = getOrDefaultSqsDlqClient(queueId, queueConfig, hmppsSqsProperties)
        val sqsClient = getOrDefaultSqsClient(queueId, queueConfig, hmppsSqsProperties, sqsDlqClient)
          .also { subscribeToLocalStackTopic(hmppsSqsProperties, queueConfig, hmppsTopics) }
        HmppsQueue(queueId, sqsClient, queueConfig.queueName, sqsDlqClient, queueConfig.dlqName.ifEmpty { null })
          .also { getOrDefaultHealthIndicator(it) }
          .also { createJmsListenerContainerFactory(it, hmppsSqsProperties) }
      }.toList()

  private fun getOrDefaultSqsDlqClient(queueId: String, queueConfig: HmppsSqsProperties.QueueConfig, hmppsSqsProperties: HmppsSqsProperties): AmazonSQS? =
    if (queueConfig.dlqName.isNotEmpty()) {
      getOrDefaultBean("$queueId-sqs-dlq-client") {
        createSqsDlqClient(queueId, queueConfig, hmppsSqsProperties)
      }
    } else null

  private fun getOrDefaultSqsClient(queueId: String, queueConfig: HmppsSqsProperties.QueueConfig, hmppsSqsProperties: HmppsSqsProperties, sqsDlqClient: AmazonSQS?): AmazonSQS =
    getOrDefaultBean("$queueId-sqs-client") {
      createSqsClient(queueId, queueConfig, hmppsSqsProperties, sqsDlqClient)
    }

  private fun getOrDefaultHealthIndicator(hmppsQueue: HmppsQueue): HealthIndicator =
    getOrDefaultBean("${hmppsQueue.id}-health") {
      HmppsQueueHealth(hmppsQueue)
    }

  private fun createJmsListenerContainerFactory(hmppsQueue: HmppsQueue, hmppsSqsProperties: HmppsSqsProperties): HmppsQueueDestinationContainerFactory =
    getOrDefaultBean("${hmppsQueue.id}-jms-listener-factory") {
      HmppsQueueDestinationContainerFactory(hmppsQueue.id, createJmsListenerContainerFactory(hmppsQueue.sqsClient, hmppsSqsProperties))
    }

  @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
  private inline fun <reified T> getOrDefaultBean(beanName: String, createDefaultBean: () -> T) =
    runCatching { context.beanFactory.getBean(beanName) as T }
      .getOrElse {
        createDefaultBean().also { bean -> context.beanFactory.registerSingleton(beanName, bean) }
      }

  fun createSqsDlqClient(queueId: String, queueConfig: HmppsSqsProperties.QueueConfig, hmppsSqsProperties: HmppsSqsProperties): AmazonSQS =
    with(hmppsSqsProperties) {
      if (queueConfig.dlqName.isEmpty()) throw MissingDlqNameException()
      when (provider) {
        "aws" -> amazonSqsFactory.awsSqsDlqClient(queueId, queueConfig.dlqName, queueConfig.dlqAccessKeyId, queueConfig.dlqSecretAccessKey, region)
        "localstack" ->
          amazonSqsFactory.localStackSqsDlqClient(queueId, queueConfig.dlqName, localstackUrl, region)
            .also { sqsDlqClient -> sqsDlqClient.createQueue(queueConfig.dlqName) }

        else -> throw IllegalStateException("Unrecognised HMPPS SQS provider $provider")
      }
    }

  fun createSqsClient(queueId: String, queueConfig: HmppsSqsProperties.QueueConfig, hmppsSqsProperties: HmppsSqsProperties, sqsDlqClient: AmazonSQS?) =
    with(hmppsSqsProperties) {
      when (provider) {
        "aws" -> amazonSqsFactory.awsSqsClient(queueId, queueConfig.queueName, queueConfig.queueAccessKeyId, queueConfig.queueSecretAccessKey, region)
        "localstack" ->
          amazonSqsFactory.localStackSqsClient(queueId, queueConfig.queueName, localstackUrl, region)
            .also { sqsClient -> createLocalStackQueue(sqsClient, sqsDlqClient, queueConfig.queueName, queueConfig.dlqName, queueConfig.dlqMaxReceiveCount) }

        else -> throw IllegalStateException("Unrecognised HMPPS SQS provider $provider")
      }
    }

  private fun createLocalStackQueue(
    sqsClient: AmazonSQS,
    sqsDlqClient: AmazonSQS?,
    queueName: String,
    dlqName: String,
    maxReceiveCount: Int,
  ) {
    if (dlqName.isEmpty() || sqsDlqClient==null) {
      sqsClient.createQueue(CreateQueueRequest(queueName))
    } else {
      sqsDlqClient.getQueueUrl(dlqName).queueUrl
        .let { dlqQueueUrl -> sqsDlqClient.getQueueAttributes(dlqQueueUrl, listOf(QueueAttributeName.QueueArn.toString())).attributes["QueueArn"] }
        .also { queueArn ->
          sqsClient.createQueue(
            CreateQueueRequest(queueName).withAttributes(
              mapOf(
                QueueAttributeName.RedrivePolicy.toString() to
                  """{"deadLetterTargetArn":"$queueArn","maxReceiveCount":"$maxReceiveCount"}"""
              )
            )
          )
        }
    }
  }

  private fun subscribeToLocalStackTopic(hmppsSqsProperties: HmppsSqsProperties, queueConfig: HmppsSqsProperties.QueueConfig, hmppsTopics: List<HmppsTopic>) {
    if (hmppsSqsProperties.provider=="localstack")
      hmppsTopics.firstOrNull { topic -> topic.id==queueConfig.subscribeTopicId }
        ?.also { topic ->
          val subscribeAttribute = if (queueConfig.subscribeFilter.isNullOrEmpty()) mapOf() else mapOf("FilterPolicy" to queueConfig.subscribeFilter)
          topic.snsClient.subscribe(
            SubscribeRequest()
              .withTopicArn(topic.arn)
              .withProtocol("sqs")
              .withEndpoint("${hmppsSqsProperties.localstackUrl}/queue/${queueConfig.queueName}")
              .withAttributes(subscribeAttribute)
          )
            .also { log.info("Queue ${queueConfig.queueName} has subscribed to topic with arn ${topic.arn}") }
        }
  }

  fun createJmsListenerContainerFactory(awsSqsClient: AmazonSQS, hmppsSqsProperties: HmppsSqsProperties): DefaultJmsListenerContainerFactory =
    DefaultJmsListenerContainerFactory().apply {
      setConnectionFactory(SQSConnectionFactory(ProviderConfiguration(), awsSqsClient))
      setDestinationResolver(HmppsQueueDestinationResolver(hmppsSqsProperties))
      setConcurrency("1-1")
      setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE)
      setErrorHandler { t: Throwable? -> log.error("Error caught in jms listener", t) }
    }
}

class MissingDlqNameException() : RuntimeException("Attempted to access dlq but no name has been set")
