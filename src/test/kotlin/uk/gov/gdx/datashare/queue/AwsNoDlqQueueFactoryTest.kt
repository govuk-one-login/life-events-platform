package uk.gov.gdx.datashare.queue

import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.ConfigurableApplicationContext
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse
import software.amazon.awssdk.services.sqs.model.QueueAttributeName

class AwsNoDlqQueueFactoryTest {

  private val context = mockk<ConfigurableApplicationContext>()
  private val beanFactory = mockk<ConfigurableListableBeanFactory>(relaxed = true)
  private val sqsFactory = mockk<AmazonSqsFactory>()
  private val awsQueueFactory = AwsQueueFactory(context, sqsFactory)

  init {
    every { context.beanFactory }.returns(beanFactory)
  }

  @Nested
  inner class `Create single AWS AwsQueue with no dlq` {
    private val someQueueConfig = SqsProperties.QueueConfig(
      queueName = "some queue name",
    )
    private val sqsProperties = SqsProperties(queues = mapOf("somequeueid" to someQueueConfig))
    private val sqsClient = mockk<SqsClient>()
    private lateinit var awsQueues: List<AwsQueue>

    @BeforeEach
    fun `configure mocks and register queues`() {
      every { sqsFactory.awsSqsClient(any(), any(), any()) }
        .returns(sqsClient)
      every { sqsClient.getQueueUrl(any<GetQueueUrlRequest>()) }.returns(
        GetQueueUrlResponse.builder().queueUrl("some queue url").build(),
      )

      awsQueues = awsQueueFactory.createAwsQueues(sqsProperties)
    }

    @Test
    fun `creates aws sqs client but does not create aws sqs dlq client from sqs factory`() {
      verify { sqsFactory.awsSqsClient("somequeueid", "some queue name", "eu-west-2") }
      confirmVerified(sqsFactory)
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
      verify { beanFactory.registerSingleton(eq("somequeueid-health"), any<AwsQueueHealth>()) }
    }

    @Test
    fun `should register the sqs client but not the dlq client`() {
      verify {
        beanFactory.registerSingleton("somequeueid-sqs-client", sqsClient)
      }
      verify(exactly = 0) { beanFactory.registerSingleton(match { it.contains("dlq-client") }, any()) }
    }

    @Test
    fun `should register the jms listener factory`() {
      verify {
        beanFactory.registerSingleton(
          eq("somequeueid-jms-listener-factory"),
          any<AwsQueueDestinationContainerFactory>(),
        )
      }
    }
  }

  @Nested
  inner class `Create single LocalStack AwsQueue with no dlq` {
    private val someQueueConfig = SqsProperties.QueueConfig(queueName = "some queue name")
    private val sqsProperties = SqsProperties(provider = "localstack", queues = mapOf("somequeueid" to someQueueConfig))
    private val sqsClient = mockk<SqsClient>(relaxed = true)
    private lateinit var awsQueues: List<AwsQueue>

    @BeforeEach
    fun `configure mocks and register queues`() {
      clearMocks(sqsFactory)
      every { sqsFactory.localStackSqsClient(any(), any(), any(), any()) }
        .returns(sqsClient)
      every { sqsClient.getQueueUrl(any<GetQueueUrlRequest>()) }.returns(
        GetQueueUrlResponse.builder().queueUrl("some queue url").build(),
      )

      awsQueues = awsQueueFactory.createAwsQueues(sqsProperties)
    }

    @Test
    fun `creates LocalStack sqs client from sqs factory but not dlq client`() {
      verify {
        sqsFactory.localStackSqsClient(
          queueId = "somequeueid",
          localstackUrl = "http://localhost:4566",
          region = "eu-west-2",
          queueName = "some queue name",
        )
      }
      confirmVerified(sqsFactory)
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
      verify { beanFactory.registerSingleton(eq("somequeueid-health"), any<AwsQueueHealth>()) }
    }

    @Test
    fun `should register the sqs client but not the dlq client`() {
      verify { beanFactory.registerSingleton("somequeueid-sqs-client", sqsClient) }
      verify(exactly = 0) { beanFactory.registerSingleton(match { it.contains("dlq-client") }, any()) }
    }

    @Test
    fun `should create a queue without a redrive policy`() {
      var createQueueRequest = slot<CreateQueueRequest>()
      verify { sqsClient.createQueue(capture(createQueueRequest)) }

      assertThat(createQueueRequest.captured.attributes()).doesNotContainEntry(
        QueueAttributeName.REDRIVE_POLICY,
        """{"deadLetterTargetArn":"some dlq arn","maxReceiveCount":"5"}""",
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
    private val sqsProperties =
      SqsProperties(queues = mapOf("somequeueid" to someQueueConfig, "anotherqueueid" to anotherQueueConfig))
    private val sqsClient = mockk<SqsClient>()
    private lateinit var awsQueues: List<AwsQueue>

    @BeforeEach
    fun `configure mocks and register queues`() {
      every { sqsFactory.awsSqsClient(any(), any(), any()) }
        .returns(sqsClient)
      every { sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName("some queue name").build()) }.returns(
        GetQueueUrlResponse.builder().queueUrl("some queue url").build(),
      )
      every { sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName("another queue name").build()) }.returns(
        GetQueueUrlResponse.builder().queueUrl("another queue url").build(),
      )

      awsQueues = awsQueueFactory.createAwsQueues(sqsProperties)
    }

    @Test
    fun `should create multiple sqs clients but no dlq clients from sqs factory`() {
      verify { sqsFactory.awsSqsClient("somequeueid", "some queue name", "eu-west-2") }
      verify { sqsFactory.awsSqsClient("anotherqueueid", "another queue name", "eu-west-2") }
      confirmVerified(sqsFactory)
    }

    @Test
    fun `should return multiple queue details`() {
      assertThat(awsQueues[0].id).isEqualTo("somequeueid")
      assertThat(awsQueues[1].id).isEqualTo("anotherqueueid")
    }

    @Test
    fun `should register multiple health indicators`() {
      verify { beanFactory.registerSingleton(eq("somequeueid-health"), any<AwsQueueHealth>()) }
      verify { beanFactory.registerSingleton(eq("anotherqueueid-health"), any<AwsQueueHealth>()) }
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
    private val sqsClient = mockk<SqsClient>(relaxed = true)
    private lateinit var awsQueues: List<AwsQueue>

    @BeforeEach
    fun `configure mocks and register queues`() {
      every { sqsFactory.localStackSqsClient(any(), any(), any(), any()) }
        .returns(sqsClient)
      every { sqsClient.getQueueUrl(any<GetQueueUrlRequest>()) }.returns(
        GetQueueUrlResponse.builder().queueUrl("some queue url").build(),
      )

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
    private val sqsClient = mockk<SqsClient>()
    private lateinit var awsQueues: List<AwsQueue>

    @BeforeEach
    fun `configure mocks and register queues`() {
      every { sqsFactory.awsSqsClient(any(), any(), any()) }
        .returns(sqsClient)
      every { sqsClient.getQueueUrl(any<GetQueueUrlRequest>()) }.returns(
        GetQueueUrlResponse.builder().queueUrl("some queue url").build(),
      )

      awsQueues = awsQueueFactory.createAwsQueues(sqsProperties)
    }

    @Test
    fun `should return the queue AmazonSQS client`() {
      assertThat(awsQueues[0].sqsClient).isEqualTo(sqsClient)
    }
  }
}
