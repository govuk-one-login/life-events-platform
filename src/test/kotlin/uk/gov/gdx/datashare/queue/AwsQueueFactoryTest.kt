package uk.gov.gdx.datashare.queue

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.CreateQueueRequest
import com.amazonaws.services.sqs.model.GetQueueAttributesResult
import com.amazonaws.services.sqs.model.GetQueueUrlResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.*
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.ConfigurableApplicationContext

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
    private val sqsClient = mock<AmazonSQS>()
    private val sqsDlqClient = mock<AmazonSQS>()
    private lateinit var awsQueues: List<AwsQueue>

    @BeforeEach
    fun `configure mocks and register queues`() {
      whenever(sqsFactory.awsSqsDlqClient(anyString(), anyString(), anyString()))
        .thenReturn(sqsDlqClient)
      whenever(sqsFactory.awsSqsClient(anyString(), anyString(), anyString()))
        .thenReturn(sqsClient)
      whenever(sqsDlqClient.getQueueUrl(anyString())).thenReturn(GetQueueUrlResult().withQueueUrl("some dlq url"))
      whenever(sqsClient.getQueueUrl(anyString())).thenReturn(GetQueueUrlResult().withQueueUrl("some queue url"))

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
    private val sqsClient = mock<AmazonSQS>()
    private val sqsDlqClient = mock<AmazonSQS>()
    private lateinit var awsQueues: List<AwsQueue>

    @BeforeEach
    fun `configure mocks and register queues`() {
      whenever(sqsFactory.localStackSqsDlqClient(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(sqsDlqClient)
      whenever(sqsFactory.localStackSqsClient(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(sqsClient)
      whenever(sqsClient.getQueueUrl(anyString())).thenReturn(GetQueueUrlResult().withQueueUrl("some queue url"))
      whenever(sqsDlqClient.getQueueUrl(anyString())).thenReturn(GetQueueUrlResult().withQueueUrl("some dlq url"))
      whenever(sqsDlqClient.getQueueAttributes(anyString(), anyList())).thenReturn(GetQueueAttributesResult().withAttributes(mapOf("QueueArn" to "some dlq arn")))

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
      verify(sqsDlqClient).getQueueAttributes("some dlq url", listOf("QueueArn"))
    }

    @Test
    fun `should create a queue with a redrive policy`() {
      verify(sqsClient).createQueue(
        check<CreateQueueRequest> {
          assertThat(it.attributes).containsEntry("RedrivePolicy", """{"deadLetterTargetArn":"some dlq arn","maxReceiveCount":"5"}""")
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
          assertThat(it.attributes).containsEntry("RedrivePolicy", """{"deadLetterTargetArn":"some dlq arn","maxReceiveCount":"2"}""")
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
    private val sqsClient = mock<AmazonSQS>()
    private val sqsDlqClient = mock<AmazonSQS>()
    private lateinit var awsQueues: List<AwsQueue>

    @BeforeEach
    fun `configure mocks and register queues`() {
      whenever(sqsFactory.awsSqsDlqClient(anyString(), anyString(), anyString()))
        .thenReturn(sqsDlqClient)
      whenever(sqsFactory.awsSqsClient(anyString(), anyString(), anyString()))
        .thenReturn(sqsClient)
      whenever(sqsClient.getQueueUrl("some queue name")).thenReturn(GetQueueUrlResult().withQueueUrl("some queue url"))
      whenever(sqsDlqClient.getQueueUrl("some dlq name")).thenReturn(GetQueueUrlResult().withQueueUrl("some dlq url"))
      whenever(sqsClient.getQueueUrl("another queue name")).thenReturn(GetQueueUrlResult().withQueueUrl("another queue url"))
      whenever(sqsDlqClient.getQueueUrl("another dlq name")).thenReturn(GetQueueUrlResult().withQueueUrl("another dlq url"))

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
    private val someTopicConfig = SqsProperties.TopicConfig(
      arn = "${localstackArnPrefix}some-topic-name",
    )
    private val sqsProperties = SqsProperties(provider = "localstack", queues = mapOf("somequeueid" to someQueueConfig), topics = mapOf("sometopicid" to someTopicConfig))
    private val sqsClient = mock<AmazonSQS>()
    private val sqsDlqClient = mock<AmazonSQS>()
    private val snsClient = mock<AmazonSNS>()
    private val topics = listOf(AwsTopic(id = "sometopicid", arn = "some topic arn", snsClient = snsClient))
    private lateinit var awsQueues: List<AwsQueue>

    @BeforeEach
    fun `configure mocks and register queues`() {
      whenever(sqsFactory.localStackSqsDlqClient(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(sqsDlqClient)
      whenever(sqsFactory.localStackSqsClient(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(sqsClient)
      whenever(sqsClient.getQueueUrl(anyString())).thenReturn(GetQueueUrlResult().withQueueUrl("some queue url"))
      whenever(sqsDlqClient.getQueueUrl(anyString())).thenReturn(GetQueueUrlResult().withQueueUrl("some dlq url"))
      whenever(sqsDlqClient.getQueueAttributes(anyString(), anyList())).thenReturn(GetQueueAttributesResult().withAttributes(mapOf("QueueArn" to "some dlq arn")))

      awsQueues = awsQueueFactory.createAwsQueues(sqsProperties, topics)
    }

    @Test
    fun `should return the queue AmazonSQS client`() {
      assertThat(awsQueues[0].sqsClient).isEqualTo(sqsClient)
    }

    @Test
    fun `should subscribe to the topic`() {
      verify(snsClient).subscribe(
        check { subscribeRequest ->
          assertThat(subscribeRequest.topicArn).isEqualTo("some topic arn")
          assertThat(subscribeRequest.protocol).isEqualTo("sqs")
          assertThat(subscribeRequest.endpoint).isEqualTo("http://localhost:4566/queue/some-queue-name")
          assertThat(subscribeRequest.attributes["FilterPolicy"]).isEqualTo("some topic filter")
        },
      )
    }
  }

  @Nested
  inner class `Create AWS AwsQueue with topic subscription` {
    private val someQueueConfig = SqsProperties.QueueConfig(subscribeTopicId = "sometopicid", subscribeFilter = "some topic filter", queueName = "some queue name", dlqName = "some dlq name")
    private val someTopicConfig = SqsProperties.TopicConfig(arn = "some topic arn")
    private val sqsProperties = SqsProperties(queues = mapOf("somequeueid" to someQueueConfig), topics = mapOf("sometopicid" to someTopicConfig))
    private val sqsClient = mock<AmazonSQS>()
    private val sqsDlqClient = mock<AmazonSQS>()
    private val snsClient = mock<AmazonSNS>()
    private val topics = listOf(AwsTopic(id = "sometopicid", arn = "some topic arn", snsClient = snsClient))
    private lateinit var awsQueues: List<AwsQueue>

    @BeforeEach
    fun `configure mocks and register queues`() {
      whenever(sqsFactory.awsSqsDlqClient(anyString(), anyString(), anyString()))
        .thenReturn(sqsDlqClient)
      whenever(sqsFactory.awsSqsClient(anyString(), anyString(), anyString()))
        .thenReturn(sqsClient)
      whenever(sqsDlqClient.getQueueUrl(anyString())).thenReturn(GetQueueUrlResult().withQueueUrl("some dlq url"))
      whenever(sqsClient.getQueueUrl(anyString())).thenReturn(GetQueueUrlResult().withQueueUrl("some queue url"))

      awsQueues = awsQueueFactory.createAwsQueues(sqsProperties, topics)
    }

    @Test
    fun `should return the queue AmazonSQS client`() {
      assertThat(awsQueues[0].sqsClient).isEqualTo(sqsClient)
    }

    @Test
    fun `should not subscribe to the topic`() {
      verifyNoMoreInteractions(snsClient)
    }
  }
}
