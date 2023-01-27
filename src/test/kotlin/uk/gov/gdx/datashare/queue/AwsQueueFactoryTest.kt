package uk.gov.gdx.datashare.queue

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.*
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.ConfigurableApplicationContext
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.*

class AwsQueueFactoryTest {

  private val localstackArnPrefix = "arn:aws:sns:eu-west-2:000000000000:"

  private val context = mock<ConfigurableApplicationContext>()
  private val beanFactory = mock<ConfigurableListableBeanFactory>()
  private val sqsFactory = mock<AmazonSqsFactory>()
  private val awsQueueFactory = AwsQueueFactory(context, sqsFactory)

  init {
    whenever(context.beanFactory).thenReturn(beanFactory)
  }

  @Nested
  inner class `Create single AWS AwsQueue` {
    private val someQueueConfig = SqsProperties.QueueConfig(
      queueName = "some queue name",
      dlqName = "some dlq name",
    )
    private val sqsProperties = SqsProperties(queues = mapOf("somequeueid" to someQueueConfig))
    private val sqsClient = mock<SqsClient>()
    private val sqsDlqClient = mock<SqsClient>()
    private lateinit var awsQueues: List<AwsQueue>

    @BeforeEach
    fun `configure mocks and register queues`() {
      whenever(sqsFactory.awsSqsDlqClient(anyString(), anyString(), anyString()))
        .thenReturn(sqsDlqClient)
      whenever(sqsFactory.awsSqsClient(anyString(), anyString(), anyString()))
        .thenReturn(sqsClient)
      whenever(sqsDlqClient.getQueueUrl(any<GetQueueUrlRequest>())).thenReturn(GetQueueUrlResponse.builder().queueUrl("some dlq url").build())
      whenever(sqsClient.getQueueUrl(any<GetQueueUrlRequest>())).thenReturn(GetQueueUrlResponse.builder().queueUrl("some queue url").build())

      awsQueues = awsQueueFactory.createAwsQueues(sqsProperties)
    }

    @Test
    fun `creates aws sqs dlq client from sqs factory`() {
      verify(sqsFactory).awsSqsDlqClient("somequeueid", "some dlq name", "eu-west-2")
    }

    @Test
    fun `creates aws sqs client from sqs factory`() {
      verify(sqsFactory).awsSqsClient("somequeueid", "some queue name", "eu-west-2")
    }

    @Test
    fun `should return the queue details`() {
      assertThat(awsQueues[0].id).isEqualTo("somequeueid")
    }

    @Test
    fun `should return the queue AmazonSQS client`() {
      assertThat(awsQueues[0].sqsClient).isEqualTo(sqsClient)
    }

    @Test
    fun `should return the queue name`() {
      assertThat(awsQueues[0].queueName).isEqualTo("some queue name")
    }

    @Test
    fun `should return the queue url`() {
      assertThat(awsQueues[0].queueUrl).isEqualTo("some queue url")
    }

    @Test
    fun `should return the dlq client`() {
      assertThat(awsQueues[0].sqsDlqClient).isEqualTo(sqsDlqClient)
    }

    @Test
    fun `should return the dlq name`() {
      assertThat(awsQueues[0].dlqName).isEqualTo("some dlq name")
    }

    @Test
    fun `should return the dlq url`() {
      assertThat(awsQueues[0].dlqUrl).isEqualTo("some dlq url")
    }

    @Test
    fun `should register a health indicator`() {
      verify(beanFactory).registerSingleton(eq("somequeueid-health"), any<AwsQueueHealth>())
    }

    @Test
    fun `should register the sqs client`() {
      verify(beanFactory).registerSingleton("somequeueid-sqs-client", sqsClient)
    }

    @Test
    fun `should register the sqs dlq client`() {
      verify(beanFactory).registerSingleton("somequeueid-sqs-dlq-client", sqsDlqClient)
    }

    @Test
    fun `should register the jms listener factory`() {
      verify(beanFactory).registerSingleton(eq("somequeueid-jms-listener-factory"), any<AwsQueueDestinationContainerFactory>())
    }
  }

  @Nested
  inner class `Create single LocalStack AwsQueue` {
    private val someQueueConfig = SqsProperties.QueueConfig(queueName = "some queue name", dlqName = "some dlq name")
    private val sqsProperties = SqsProperties(provider = "localstack", queues = mapOf("somequeueid" to someQueueConfig))
    private val sqsClient = mock<SqsClient>()
    private val sqsDlqClient = mock<SqsClient>()
    private lateinit var awsQueues: List<AwsQueue>

    @BeforeEach
    fun `configure mocks and register queues`() {
      whenever(sqsFactory.localStackSqsDlqClient(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(sqsDlqClient)
      whenever(sqsFactory.localStackSqsClient(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(sqsClient)
      whenever(sqsClient.getQueueUrl(any<GetQueueUrlRequest>())).thenReturn(GetQueueUrlResponse.builder().queueUrl("some queue url").build())
      whenever(sqsDlqClient.getQueueUrl(any<GetQueueUrlRequest>())).thenReturn(GetQueueUrlResponse.builder().queueUrl("some dlq url").build())
      whenever(sqsDlqClient.getQueueAttributes(any<GetQueueAttributesRequest>())).thenReturn(GetQueueAttributesResponse.builder().attributes(mapOf(QueueAttributeName.QUEUE_ARN to "some dlq arn")).build())

      awsQueues = awsQueueFactory.createAwsQueues(sqsProperties)
    }

    @Test
    fun `creates LocalStack sqs dlq client from sqs factory`() {
      verify(sqsFactory).localStackSqsDlqClient(queueId = "somequeueid", localstackUrl = "http://localhost:4566", region = "eu-west-2", dlqName = "some dlq name")
    }

    @Test
    fun `creates LocalStack sqs client from sqs factory`() {
      verify(sqsFactory).localStackSqsClient(queueId = "somequeueid", localstackUrl = "http://localhost:4566", region = "eu-west-2", queueName = "some queue name")
    }

    @Test
    fun `should return the queue details`() {
      assertThat(awsQueues[0].id).isEqualTo("somequeueid")
    }

    @Test
    fun `should return the queue AmazonSQS client`() {
      assertThat(awsQueues[0].sqsClient).isEqualTo(sqsClient)
    }

    @Test
    fun `should return the queue name`() {
      assertThat(awsQueues[0].queueName).isEqualTo("some queue name")
    }

    @Test
    fun `should return the queue url`() {
      assertThat(awsQueues[0].queueUrl).isEqualTo("some queue url")
    }

    @Test
    fun `should return the dlq client`() {
      assertThat(awsQueues[0].sqsDlqClient).isEqualTo(sqsDlqClient)
    }

    @Test
    fun `should return the dlq name`() {
      assertThat(awsQueues[0].dlqName).isEqualTo("some dlq name")
    }

    @Test
    fun `should return the dlq url`() {
      assertThat(awsQueues[0].dlqUrl).isEqualTo("some dlq url")
    }

    @Test
    fun `should register a health indicator`() {
      verify(beanFactory).registerSingleton(eq("somequeueid-health"), any<AwsQueueHealth>())
    }

    @Test
    fun `should register the sqs client`() {
      verify(beanFactory).registerSingleton("somequeueid-sqs-client", sqsClient)
    }

    @Test
    fun `should register the sqs dlq client`() {
      verify(beanFactory).registerSingleton("somequeueid-sqs-dlq-client", sqsDlqClient)
    }

    @Test
    fun `should retrieve the dlq arn from the dlq client`() {
      verify(sqsDlqClient).getQueueAttributes(GetQueueAttributesRequest.builder().queueUrl("some dlq url").attributeNames(QueueAttributeName.QUEUE_ARN).build())
    }

    @Test
    fun `should create a queue with a redrive policy`() {
      verify(sqsClient).createQueue(
        check<CreateQueueRequest> {
          assertThat(it.attributes()).containsEntry(QueueAttributeName.REDRIVE_POLICY, """{"deadLetterTargetArn":"some dlq arn","maxReceiveCount":"5"}""")
        },
      )
    }

    @Test
    fun `should use configurable maxReceiveCount on RedrivePolicy`() {
      val someQueueConfig =
        SqsProperties.QueueConfig(queueName = "some queue name", dlqName = "some dlq name", dlqMaxReceiveCount = 2)
      val sqsProperties = SqsProperties(provider = "localstack", queues = mapOf("somequeueid" to someQueueConfig))
      awsQueues = awsQueueFactory.createAwsQueues(sqsProperties)

      verify(sqsClient).createQueue(
        check<CreateQueueRequest> {
          assertThat(it.attributes()).containsEntry(QueueAttributeName.REDRIVE_POLICY, """{"deadLetterTargetArn":"some dlq arn","maxReceiveCount":"2"}""")
        },
      )
    }
  }

  @Nested
  inner class `Create multiple AWS AwsQueues` {
    private val someQueueConfig = SqsProperties.QueueConfig(
      queueName = "some queue name",
      dlqName = "some dlq name",
    )
    private val anotherQueueConfig = SqsProperties.QueueConfig(queueName = "another queue name", dlqName = "another dlq name")
    private val sqsProperties = SqsProperties(queues = mapOf("somequeueid" to someQueueConfig, "anotherqueueid" to anotherQueueConfig))
    private val sqsClient = mock<SqsClient>()
    private val sqsDlqClient = mock<SqsClient>()
    private lateinit var awsQueues: List<AwsQueue>

    @BeforeEach
    fun `configure mocks and register queues`() {
      whenever(sqsFactory.awsSqsDlqClient(anyString(), anyString(), anyString()))
        .thenReturn(sqsDlqClient)
      whenever(sqsFactory.awsSqsClient(anyString(), anyString(), anyString()))
        .thenReturn(sqsClient)
      whenever(sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName("some queue name").build())).thenReturn(GetQueueUrlResponse.builder().queueUrl("some queue url").build())
      whenever(sqsDlqClient.getQueueUrl(GetQueueUrlRequest.builder().queueName("some dlq name").build())).thenReturn(GetQueueUrlResponse.builder().queueUrl("some dlq url").build())
      whenever(sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName("another queue name").build())).thenReturn(GetQueueUrlResponse.builder().queueUrl("another queue url").build())
      whenever(sqsDlqClient.getQueueUrl(GetQueueUrlRequest.builder().queueName("another dlq name").build())).thenReturn(GetQueueUrlResponse.builder().queueUrl("another dlq url").build())

      awsQueues = awsQueueFactory.createAwsQueues(sqsProperties)
    }

    @Test
    fun `should create multiple dlq clients from sqs factory`() {
      verify(sqsFactory).awsSqsDlqClient("somequeueid", "some dlq name", "eu-west-2")
      verify(sqsFactory).awsSqsDlqClient("anotherqueueid", "another dlq name", "eu-west-2")
    }

    @Test
    fun `should create multiple sqs clients from sqs factory`() {
      verify(sqsFactory).awsSqsClient("somequeueid", "some queue name", "eu-west-2")
      verify(sqsFactory).awsSqsClient("anotherqueueid", "another queue name", "eu-west-2")
    }

    @Test
    fun `should return multiple queue details`() {
      assertThat(awsQueues[0].id).isEqualTo("somequeueid")
      assertThat(awsQueues[1].id).isEqualTo("anotherqueueid")
    }

    @Test
    fun `should register multiple health indicators`() {
      verify(beanFactory).registerSingleton(eq("somequeueid-health"), any<AwsQueueHealth>())
      verify(beanFactory).registerSingleton(eq("anotherqueueid-health"), any<AwsQueueHealth>())
    }
  }

  @Nested
  inner class `Create LocalStack AwsQueue with topic subscription` {
    private val someQueueConfig = SqsProperties.QueueConfig(
      subscribeTopicId = "sometopicid",
      subscribeFilter = "some topic filter",
      queueName = "some-queue-name",
      dlqName = "some dlq name",
    )
    private val sqsProperties = SqsProperties(provider = "localstack", queues = mapOf("somequeueid" to someQueueConfig))
    private val sqsClient = mock<SqsClient>()
    private val sqsDlqClient = mock<SqsClient>()
    private lateinit var awsQueues: List<AwsQueue>

    @BeforeEach
    fun `configure mocks and register queues`() {
      whenever(sqsFactory.localStackSqsDlqClient(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(sqsDlqClient)
      whenever(sqsFactory.localStackSqsClient(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(sqsClient)
      whenever(sqsClient.getQueueUrl(any<GetQueueUrlRequest>())).thenReturn(GetQueueUrlResponse.builder().queueUrl("some queue url").build())
      whenever(sqsDlqClient.getQueueUrl(any<GetQueueUrlRequest>())).thenReturn(GetQueueUrlResponse.builder().queueUrl("some dlq url").build())
      whenever(sqsDlqClient.getQueueAttributes(any<GetQueueAttributesRequest>())).thenReturn(GetQueueAttributesResponse.builder().attributes(mapOf(QueueAttributeName.QUEUE_ARN to "some dlq arn")).build())

      awsQueues = awsQueueFactory.createAwsQueues(sqsProperties)
    }

    @Test
    fun `should return the queue AmazonSQS client`() {
      assertThat(awsQueues[0].sqsClient).isEqualTo(sqsClient)
    }
  }

  @Nested
  inner class `Create AWS AwsQueue with topic subscription` {
    private val someQueueConfig = SqsProperties.QueueConfig(subscribeTopicId = "sometopicid", subscribeFilter = "some topic filter", queueName = "some queue name", dlqName = "some dlq name")
    private val sqsProperties = SqsProperties(queues = mapOf("somequeueid" to someQueueConfig))
    private val sqsClient = mock<SqsClient>()
    private val sqsDlqClient = mock<SqsClient>()
    private lateinit var awsQueues: List<AwsQueue>

    @BeforeEach
    fun `configure mocks and register queues`() {
      whenever(sqsFactory.awsSqsDlqClient(anyString(), anyString(), anyString()))
        .thenReturn(sqsDlqClient)
      whenever(sqsFactory.awsSqsClient(anyString(), anyString(), anyString()))
        .thenReturn(sqsClient)
      whenever(sqsDlqClient.getQueueUrl(any<GetQueueUrlRequest>())).thenReturn(GetQueueUrlResponse.builder().queueUrl("some dlq url").build())
      whenever(sqsClient.getQueueUrl(any<GetQueueUrlRequest>())).thenReturn(GetQueueUrlResponse.builder().queueUrl("some queue url").build())

      awsQueues = awsQueueFactory.createAwsQueues(sqsProperties)
    }

    @Test
    fun `should return the queue AmazonSQS client`() {
      assertThat(awsQueues[0].sqsClient).isEqualTo(sqsClient)
    }
  }
}
