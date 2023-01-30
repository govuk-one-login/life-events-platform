package uk.gov.gdx.datashare.queue

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.contains
import org.mockito.kotlin.*
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.ConfigurableApplicationContext
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse
import software.amazon.awssdk.services.sqs.model.QueueAttributeName

class AwsNoDlqQueueFactoryTest {

  private val context = mock<ConfigurableApplicationContext>()
  private val beanFactory = mock<ConfigurableListableBeanFactory>()
  private val sqsFactory = mock<AmazonSqsFactory>()
  private val awsQueueFactory = AwsQueueFactory(context, sqsFactory)

  init {
    whenever(context.beanFactory).thenReturn(beanFactory)
  }

  @Nested
  inner class `Create single AWS AwsQueue with no dlq` {
    private val someQueueConfig = SqsProperties.QueueConfig(
      queueName = "some queue name",
    )
    private val sqsProperties = SqsProperties(queues = mapOf("somequeueid" to someQueueConfig))
    private val sqsClient = mock<SqsClient>()
    private lateinit var awsQueues: List<AwsQueue>

    @BeforeEach
    fun `configure mocks and register queues`() {
      whenever(sqsFactory.awsSqsClient(anyString(), anyString(), anyString()))
        .thenReturn(sqsClient)
      whenever(sqsClient.getQueueUrl(any<GetQueueUrlRequest>())).thenReturn(GetQueueUrlResponse.builder().queueUrl("some queue url").build())

      awsQueues = awsQueueFactory.createAwsQueues(sqsProperties)
    }

    @Test
    fun `creates aws sqs client but does not create aws sqs dlq client from sqs factory`() {
      verify(sqsFactory).awsSqsClient("somequeueid", "some queue name", "eu-west-2")
      verifyNoMoreInteractions(sqsFactory)
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
    fun `should not return dlq client`() {
      assertThat(awsQueues[0].sqsDlqClient).isNull()
    }

    @Test
    fun `should not return dlq name`() {
      assertThat(awsQueues[0].dlqName).isNull()
    }

    @Test
    fun `should not return dlq url`() {
      assertThat(awsQueues[0].dlqUrl).isNull()
    }

    @Test
    fun `should register a health indicator`() {
      verify(beanFactory).registerSingleton(eq("somequeueid-health"), any<AwsQueueHealth>())
    }

    @Test
    fun `should register the sqs client but not the dlq client`() {
      verify(beanFactory).registerSingleton("somequeueid-sqs-client", sqsClient)
      verify(beanFactory, never()).registerSingleton(contains("dlq-client"), any<SqsClient>())
    }

    @Test
    fun `should register the jms listener factory`() {
      verify(beanFactory).registerSingleton(eq("somequeueid-jms-listener-factory"), any<AwsQueueDestinationContainerFactory>())
    }
  }

  @Nested
  inner class `Create single LocalStack AwsQueue with no dlq` {
    private val someQueueConfig = SqsProperties.QueueConfig(queueName = "some queue name")
    private val sqsProperties = SqsProperties(provider = "localstack", queues = mapOf("somequeueid" to someQueueConfig))
    private val sqsClient = mock<SqsClient>()
    private lateinit var awsQueues: List<AwsQueue>

    @BeforeEach
    fun `configure mocks and register queues`() {
      whenever(sqsFactory.localStackSqsClient(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(sqsClient)
      whenever(sqsClient.getQueueUrl(any<GetQueueUrlRequest>())).thenReturn(GetQueueUrlResponse.builder().queueUrl("some queue url").build())

      awsQueues = awsQueueFactory.createAwsQueues(sqsProperties)
    }

    @Test
    fun `creates LocalStack sqs client from sqs factory but not dlq client`() {
      verify(sqsFactory).localStackSqsClient(queueId = "somequeueid", localstackUrl = "http://localhost:4566", region = "eu-west-2", queueName = "some queue name")
      verifyNoMoreInteractions(sqsFactory)
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
    fun `should not return a dlq client`() {
      assertThat(awsQueues[0].sqsDlqClient).isNull()
    }

    @Test
    fun `should not return a dlq name`() {
      assertThat(awsQueues[0].dlqName).isNull()
    }

    @Test
    fun `should not return a dlq url`() {
      assertThat(awsQueues[0].dlqUrl).isNull()
    }

    @Test
    fun `should register a health indicator`() {
      verify(beanFactory).registerSingleton(eq("somequeueid-health"), any<AwsQueueHealth>())
    }

    @Test
    fun `should register the sqs client but not the dlq client`() {
      verify(beanFactory).registerSingleton("somequeueid-sqs-client", sqsClient)
      verify(beanFactory, never()).registerSingleton(contains("dlq-client"), any<SqsClient>())
    }

    @Test
    fun `should create a queue without a redrive policy`() {
      verify(sqsClient).createQueue(
        check<CreateQueueRequest> {
          assertThat(it.attributes()).doesNotContainEntry(QueueAttributeName.REDRIVE_POLICY, """{"deadLetterTargetArn":"some dlq arn","maxReceiveCount":"5"}""")
        },
      )
    }
  }

  @Nested
  inner class `Create multiple AWS AwsQueues without dlqs` {
    private val someQueueConfig = SqsProperties.QueueConfig(
      queueName = "some queue name",
    )
    private val anotherQueueConfig = SqsProperties.QueueConfig(
      queueName = "another queue name",
    )
    private val sqsProperties = SqsProperties(queues = mapOf("somequeueid" to someQueueConfig, "anotherqueueid" to anotherQueueConfig))
    private val sqsClient = mock<SqsClient>()
    private lateinit var awsQueues: List<AwsQueue>

    @BeforeEach
    fun `configure mocks and register queues`() {
      whenever(sqsFactory.awsSqsClient(anyString(), anyString(), anyString()))
        .thenReturn(sqsClient)
      whenever(sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName("some queue name").build())).thenReturn(GetQueueUrlResponse.builder().queueUrl("some queue url").build())
      whenever(sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName("another queue name").build())).thenReturn(GetQueueUrlResponse.builder().queueUrl("another queue url").build())

      awsQueues = awsQueueFactory.createAwsQueues(sqsProperties)
    }

    @Test
    fun `should create multiple sqs clients but no dlq clients from sqs factory`() {
      verify(sqsFactory).awsSqsClient("somequeueid", "some queue name", "eu-west-2")
      verify(sqsFactory).awsSqsClient("anotherqueueid", "another queue name", "eu-west-2")
      verifyNoMoreInteractions(sqsFactory)
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
    )
    private val sqsProperties = SqsProperties(provider = "localstack", queues = mapOf("somequeueid" to someQueueConfig))
    private val sqsClient = mock<SqsClient>()
    private lateinit var awsQueues: List<AwsQueue>

    @BeforeEach
    fun `configure mocks and register queues`() {
      whenever(sqsFactory.localStackSqsClient(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(sqsClient)
      whenever(sqsClient.getQueueUrl(any<GetQueueUrlRequest>())).thenReturn(GetQueueUrlResponse.builder().queueUrl("some queue url").build())

      awsQueues = awsQueueFactory.createAwsQueues(sqsProperties)
    }

    @Test
    fun `should return the queue AmazonSQS client`() {
      assertThat(awsQueues[0].sqsClient).isEqualTo(sqsClient)
    }
  }

  @Nested
  inner class `Create AWS AwsQueue with topic subscription` {
    private val someQueueConfig = SqsProperties.QueueConfig(
      subscribeTopicId = "sometopicid",
      subscribeFilter = "some topic filter",
      queueName = "some queue name",
    )
    private val sqsProperties = SqsProperties(queues = mapOf("somequeueid" to someQueueConfig))
    private val sqsClient = mock<SqsClient>()
    private lateinit var awsQueues: List<AwsQueue>

    @BeforeEach
    fun `configure mocks and register queues`() {
      whenever(sqsFactory.awsSqsClient(anyString(), anyString(), anyString()))
        .thenReturn(sqsClient)
      whenever(sqsClient.getQueueUrl(any<GetQueueUrlRequest>())).thenReturn(GetQueueUrlResponse.builder().queueUrl("some queue url").build())

      awsQueues = awsQueueFactory.createAwsQueues(sqsProperties)
    }

    @Test
    fun `should return the queue AmazonSQS client`() {
      assertThat(awsQueues[0].sqsClient).isEqualTo(sqsClient)
    }
  }
}
