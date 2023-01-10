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

class AwsQueueFactory(
  private val context: ConfigurableApplicationContext,
  private val amazonSqsFactory: AmazonSqsFactory,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun createAwsQueues(sqsProperties: SqsProperties, awsTopics: List<AwsTopic> = listOf()) =
    sqsProperties.queues
      .map { (queueId, queueConfig) ->
        val sqsDlqClient = getOrDefaultSqsDlqClient(queueId, queueConfig, sqsProperties)
        val sqsClient = getOrDefaultSqsClient(queueId, queueConfig, sqsProperties, sqsDlqClient)
          .also { subscribeToLocalStackTopic(sqsProperties, queueConfig, awsTopics) }
        AwsQueue(queueId, sqsClient, queueConfig.queueName, sqsDlqClient, queueConfig.dlqName.ifEmpty { null })
          .also { getOrDefaultHealthIndicator(it) }
          .also { createJmsListenerContainerFactory(it, sqsProperties) }
      }.toList()

  private fun getOrDefaultSqsDlqClient(queueId: String, queueConfig: SqsProperties.QueueConfig, sqsProperties: SqsProperties): AmazonSQS? =
    if (queueConfig.dlqName.isNotEmpty()) {
      getOrDefaultBean("$queueId-sqs-dlq-client") {
        createSqsDlqClient(queueId, queueConfig, sqsProperties)
      }
    } else null

  private fun getOrDefaultSqsClient(queueId: String, queueConfig: SqsProperties.QueueConfig, sqsProperties: SqsProperties, sqsDlqClient: AmazonSQS?): AmazonSQS =
    getOrDefaultBean("$queueId-sqs-client") {
      createSqsClient(queueId, queueConfig, sqsProperties, sqsDlqClient)
    }

  private fun getOrDefaultHealthIndicator(awsQueue: AwsQueue): HealthIndicator =
    getOrDefaultBean("${awsQueue.id}-health") {
      AwsQueueHealth(awsQueue)
    }

  private fun createJmsListenerContainerFactory(awsQueue: AwsQueue, sqsProperties: SqsProperties): AwsQueueDestinationContainerFactory =
    getOrDefaultBean("${awsQueue.id}-jms-listener-factory") {
      AwsQueueDestinationContainerFactory(awsQueue.id, createJmsListenerContainerFactory(awsQueue.sqsClient, sqsProperties))
    }

  @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
  private inline fun <reified T> getOrDefaultBean(beanName: String, createDefaultBean: () -> T) =
    runCatching { context.beanFactory.getBean(beanName) as T }
      .getOrElse {
        createDefaultBean().also { bean -> context.beanFactory.registerSingleton(beanName, bean) }
      }

  fun createSqsDlqClient(queueId: String, queueConfig: SqsProperties.QueueConfig, sqsProperties: SqsProperties): AmazonSQS =
    with(sqsProperties) {
      if (queueConfig.dlqName.isEmpty()) throw MissingDlqNameException()
      when (provider) {
        "aws" -> amazonSqsFactory.awsSqsDlqClient(queueId, queueConfig.dlqName, region)
        "localstack" ->
          amazonSqsFactory.localStackSqsDlqClient(queueId, queueConfig.dlqName, localstackUrl, region)
            .also { sqsDlqClient -> sqsDlqClient.createQueue(queueConfig.dlqName) }

        else -> throw IllegalStateException("Unrecognised SQS provider $provider")
      }
    }

  fun createSqsClient(queueId: String, queueConfig: SqsProperties.QueueConfig, sqsProperties: SqsProperties, sqsDlqClient: AmazonSQS?) =
    with(sqsProperties) {
      when (provider) {
        "aws" -> amazonSqsFactory.awsSqsClient(queueId, queueConfig.queueName, region)
        "localstack" ->
          amazonSqsFactory.localStackSqsClient(queueId, queueConfig.queueName, localstackUrl, region)
            .also { sqsClient -> createLocalStackQueue(sqsClient, sqsDlqClient, queueConfig.queueName, queueConfig.dlqName, queueConfig.dlqMaxReceiveCount) }

        else -> throw IllegalStateException("Unrecognised SQS provider $provider")
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

  private fun subscribeToLocalStackTopic(sqsProperties: SqsProperties, queueConfig: SqsProperties.QueueConfig, awsTopics: List<AwsTopic>) {
    if (sqsProperties.provider=="localstack")
      awsTopics.firstOrNull { topic -> topic.id==queueConfig.subscribeTopicId }
        ?.also { topic ->
          val subscribeAttribute = if (queueConfig.subscribeFilter.isNullOrEmpty()) mapOf() else mapOf("FilterPolicy" to queueConfig.subscribeFilter)
          topic.snsClient.subscribe(
            SubscribeRequest()
              .withTopicArn(topic.arn)
              .withProtocol("sqs")
              .withEndpoint("${sqsProperties.localstackUrl}/queue/${queueConfig.queueName}")
              .withAttributes(subscribeAttribute)
          )
            .also { log.info("Queue ${queueConfig.queueName} has subscribed to topic with arn ${topic.arn}") }
        }
  }

  fun createJmsListenerContainerFactory(awsSqsClient: AmazonSQS, sqsProperties: SqsProperties): DefaultJmsListenerContainerFactory =
    DefaultJmsListenerContainerFactory().apply {
      setConnectionFactory(SQSConnectionFactory(ProviderConfiguration(), awsSqsClient))
      setDestinationResolver(AwsQueueDestinationResolver(sqsProperties))
      setConcurrency("1-1")
      setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE)
      setErrorHandler { t: Throwable? -> log.error("Error caught in jms listener", t) }
    }
}

class MissingDlqNameException() : RuntimeException("Attempted to access dlq but no name has been set")
