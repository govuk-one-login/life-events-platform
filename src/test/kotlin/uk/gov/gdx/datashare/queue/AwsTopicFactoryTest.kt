package uk.gov.gdx.datashare.queue

import com.amazonaws.services.sns.AmazonSNS
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.*
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.context.ConfigurableApplicationContext

@Suppress("ClassName")
class AwsTopicFactoryTest {

  private val localstackArnPrefix = "arn:aws:sns:eu-west-2:000000000000:"

  private val context = mock<ConfigurableApplicationContext>()
  private val beanFactory = mock<ConfigurableListableBeanFactory>()
  private val snsFactory = mock<AmazonSnsFactory>()
  private val awsTopicFactory = AwsTopicFactory(context, snsFactory)

  init {
    whenever(context.beanFactory).thenReturn(beanFactory)
  }

  @Nested
  inner class `Create AWS AwsTopic` {
    private val someTopicConfig =
      SqsProperties.TopicConfig(
        arn = "some arn",
      )
    private val sqsProperties =
      SqsProperties(queues = mock(), topics = mapOf("sometopicid" to someTopicConfig))
    private val snsClient = mock<AmazonSNS>()
    private lateinit var awsTopics: List<AwsTopic>

    @BeforeEach
    fun `configure mocks and register queues`() {
      whenever(snsFactory.awsSnsClient(anyString(), anyString()))
        .thenReturn(snsClient)

      awsTopics = awsTopicFactory.createAwsTopics(sqsProperties)
    }

    @Test
    fun `should create aws sns client`() {
      verify(snsFactory).awsSnsClient("sometopicid", "eu-west-2")
    }

    @Test
    fun `should return the topic details`() {
      assertThat(awsTopics[0].id).isEqualTo("sometopicid")
    }

    @Test
    fun `should return the AmazonSNS client`() {
      assertThat(awsTopics[0].snsClient).isEqualTo(snsClient)
    }

    @Test
    fun `should register the AmazonSNS client`() {
      verify(beanFactory).registerSingleton("sometopicid-sns-client", snsClient)
    }

    @Test
    fun `should register health indicators`() {
      verify(beanFactory).registerSingleton(eq("sometopicid-health"), any<HealthIndicator>())
    }
  }

  @Nested
  inner class `Create LocalStack AwsTopic` {
    private val someTopicConfig = SqsProperties.TopicConfig(
      arn = "${localstackArnPrefix}some-topic-name"
    )
    private val sqsProperties =
      SqsProperties(provider = "localstack", queues = mock(), topics = mapOf("sometopicid" to someTopicConfig))
    private val snsClient = mock<AmazonSNS>()
    private lateinit var AwsTopics: List<AwsTopic>

    @BeforeEach
    fun `configure mocks and register queues`() {
      whenever(snsFactory.localstackSnsClient(anyString(), anyString(), anyString()))
        .thenReturn(snsClient)

      AwsTopics = awsTopicFactory.createAwsTopics(sqsProperties)
    }

    @Test
    fun `should create localstack sns client`() {
      verify(snsFactory).localstackSnsClient("sometopicid", "http://localhost:4566", "eu-west-2")
    }

    @Test
    fun `should return the topic details`() {
      assertThat(AwsTopics[0].id).isEqualTo("sometopicid")
    }

    @Test
    fun `should return the AmazonSNS client`() {
      assertThat(AwsTopics[0].snsClient).isEqualTo(snsClient)
    }

    @Test
    fun `should register the AmazonSNS client`() {
      verify(beanFactory).registerSingleton("sometopicid-sns-client", snsClient)
    }

    @Test
    fun `should create the topic`() {
      verify(snsClient).createTopic("some-topic-name")
    }

    @Test
    fun `should register health indicators`() {
      verify(beanFactory).registerSingleton(eq("sometopicid-health"), any<HealthIndicator>())
    }
  }

  @Nested
  inner class `Create multiple AWS AwsTopics` {
    private val someTopicConfig =
      SqsProperties.TopicConfig(
        arn = "some arn"
      )
    private val anotherTopicConfig = SqsProperties.TopicConfig(
      arn = "another arn"
    )
    private val sqsProperties = SqsProperties(
      queues = mock(),
      topics = mapOf("sometopicid" to someTopicConfig, "anothertopicid" to anotherTopicConfig)
    )
    private val snsClient = mock<AmazonSNS>()
    private lateinit var AwsTopics: List<AwsTopic>

    @BeforeEach
    fun `configure mocks and register queues`() {
      whenever(snsFactory.awsSnsClient(anyString(), anyString()))
        .thenReturn(snsClient)
        .thenReturn(snsClient)

      AwsTopics = awsTopicFactory.createAwsTopics(sqsProperties)
    }

    @Test
    fun `should create 2 aws sns clients`() {
      verify(snsFactory).awsSnsClient("sometopicid", "eu-west-2")
      verify(snsFactory).awsSnsClient(
        "anothertopicid",
        "eu-west-2"
      )
    }

    @Test
    fun `should return the topic details`() {
      assertThat(AwsTopics[0].id).isEqualTo("sometopicid")
      assertThat(AwsTopics[1].id).isEqualTo("anothertopicid")
    }

    @Test
    fun `should return 2 AmazonSNS client`() {
      assertThat(AwsTopics[0].snsClient).isEqualTo(snsClient)
      assertThat(AwsTopics[1].snsClient).isEqualTo(snsClient)
    }

    @Test
    fun `should register 2 AmazonSNS clients`() {
      verify(beanFactory).registerSingleton("sometopicid-sns-client", snsClient)
      verify(beanFactory).registerSingleton("anothertopicid-sns-client", snsClient)
    }

    @Test
    fun `should register 2 health indicators`() {
      verify(beanFactory).registerSingleton(eq("sometopicid-health"), any<HealthIndicator>())
      verify(beanFactory).registerSingleton(eq("anothertopicid-health"), any<HealthIndicator>())
    }
  }

  @Nested
  inner class `Create multiple LocalStack AwsTopics` {
    private val someTopicConfig = SqsProperties.TopicConfig(
      arn = "${localstackArnPrefix}some arn"
    )
    private val anotherTopicConfig = SqsProperties.TopicConfig(
      arn = "${localstackArnPrefix}another arn"
    )
    private val sqsProperties = SqsProperties(
      provider = "localstack",
      queues = mock(),
      topics = mapOf("sometopicid" to someTopicConfig, "anothertopicid" to anotherTopicConfig)
    )
    private val snsClient = mock<AmazonSNS>()
    private lateinit var AwsTopics: List<AwsTopic>

    @BeforeEach
    fun `configure mocks and register queues`() {
      whenever(snsFactory.localstackSnsClient(anyString(), anyString(), anyString()))
        .thenReturn(snsClient)
        .thenReturn(snsClient)

      AwsTopics = awsTopicFactory.createAwsTopics(sqsProperties)
    }

    @Test
    fun `should create 2 aws sns clients`() {
      verify(snsFactory).localstackSnsClient("sometopicid", "http://localhost:4566", "eu-west-2")
      verify(snsFactory).localstackSnsClient("anothertopicid", "http://localhost:4566", "eu-west-2")
    }

    @Test
    fun `should return the topic details`() {
      assertThat(AwsTopics[0].id).isEqualTo("sometopicid")
      assertThat(AwsTopics[1].id).isEqualTo("anothertopicid")
    }

    @Test
    fun `should return 2 AmazonSNS client`() {
      assertThat(AwsTopics[0].snsClient).isEqualTo(snsClient)
      assertThat(AwsTopics[1].snsClient).isEqualTo(snsClient)
    }

    @Test
    fun `should register 2 AmazonSNS clients`() {
      verify(beanFactory).registerSingleton("sometopicid-sns-client", snsClient)
      verify(beanFactory).registerSingleton("anothertopicid-sns-client", snsClient)
    }

    @Test
    fun `should register 2 health indicators`() {
      verify(beanFactory).registerSingleton(eq("sometopicid-health"), any<HealthIndicator>())
      verify(beanFactory).registerSingleton(eq("anothertopicid-health"), any<HealthIndicator>())
    }
  }
}
