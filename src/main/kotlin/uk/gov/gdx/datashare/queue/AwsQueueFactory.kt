package uk.gov.gdx.datashare.queue

import com.amazon.sqs.javamessaging.ProviderConfiguration
import com.amazon.sqs.javamessaging.SQSConnectionFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.jms.config.DefaultJmsListenerContainerFactory
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest
import software.amazon.awssdk.services.sqs.model.QueueAttributeName
import javax.jms.Session

class AwsQueueFactory(
  private val context: ConfigurableApplicationContext,
  private val amazonSqsFactory: AmazonSqsFactory,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun createAwsQueues(sqsProperties: SqsProperties) =
    sqsProperties.enabledQueues
      .map { (queueId, queueConfig) ->
        val sqsDlqClient = getOrDefaultSqsDlqClient(queueId, queueConfig, sqsProperties)
        val sqsClient = getOrDefaultSqsClient(queueId, queueConfig, sqsProperties, sqsDlqClient)
        AwsQueue(queueId, sqsClient, queueConfig.queueName, sqsDlqClient, queueConfig.dlqName.ifEmpty { null }, queueConfig.awsAccountId.ifEmpty { null })
          .also { getOrDefaultHealthIndicator(it) }
          .also { createJmsListenerContainerFactory(it, sqsProperties) }
      }.toList()

  private fun getOrDefaultSqsDlqClient(queueId: String, queueConfig: SqsProperties.QueueConfig, sqsProperties: SqsProperties): SqsClient? =
    if (queueConfig.dlqName.isNotEmpty()) {
      getOrDefaultBean("$queueId-sqs-dlq-client") {
        createSqsDlqClient(queueId, queueConfig, sqsProperties)
      }
    } else {
      null
    }

  private fun getOrDefaultSqsClient(queueId: String, queueConfig: SqsProperties.QueueConfig, sqsProperties: SqsProperties, sqsDlqClient: SqsClient?): SqsClient =
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

  fun createSqsDlqClient(queueId: String, queueConfig: SqsProperties.QueueConfig, sqsProperties: SqsProperties): SqsClient =
    with(sqsProperties) {
      if (queueConfig.dlqName.isEmpty()) throw MissingDlqNameException()
      when (provider) {
        "aws" -> amazonSqsFactory.awsSqsDlqClient(queueId, queueConfig.dlqName, region)
        "localstack" ->
          amazonSqsFactory.localStackSqsDlqClient(queueId, queueConfig.dlqName, localstackUrl, region)
            .also { sqsDlqClient -> sqsDlqClient.createQueue(CreateQueueRequest.builder().queueName(queueConfig.dlqName).build()) }

        else -> throw IllegalStateException("Unrecognised SQS provider $provider")
      }
    }

  fun createSqsClient(queueId: String, queueConfig: SqsProperties.QueueConfig, sqsProperties: SqsProperties, sqsDlqClient: SqsClient?) =
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
    sqsClient: SqsClient,
    sqsDlqClient: SqsClient?,
    queueName: String,
    dlqName: String,
    maxReceiveCount: Int,
  ) {
    if (dlqName.isEmpty() || sqsDlqClient == null) {
      sqsClient.createQueue(CreateQueueRequest.builder().queueName(queueName).build())
    } else {
      sqsDlqClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(dlqName).build()).queueUrl()
        .let { dlqQueueUrl -> sqsDlqClient.getQueueAttributes(GetQueueAttributesRequest.builder().queueUrl(dlqQueueUrl).attributeNames(QueueAttributeName.QUEUE_ARN).build()).attributes()[QueueAttributeName.QUEUE_ARN] }
        .also { queueArn ->
          sqsClient.createQueue(
            CreateQueueRequest.builder()
              .queueName(queueName)
              .attributes(
                mapOf(
                  QueueAttributeName.REDRIVE_POLICY to
                    """{"deadLetterTargetArn":"$queueArn","maxReceiveCount":"$maxReceiveCount"}""",
                ),
              ).build(),
          )
        }
    }
  }

  fun createJmsListenerContainerFactory(awsSqsClient: SqsClient, sqsProperties: SqsProperties): DefaultJmsListenerContainerFactory =
    DefaultJmsListenerContainerFactory().apply {
      setConnectionFactory(SQSConnectionFactory(ProviderConfiguration(), awsSqsClient))
      setDestinationResolver(AwsQueueDestinationResolver(sqsProperties))
      setConcurrency("1-1")
      setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE)
      setErrorHandler { t: Throwable? -> log.error("Error caught in jms listener", t) }
    }
}

class MissingDlqNameException() : RuntimeException("Attempted to access dlq but no name has been set")
