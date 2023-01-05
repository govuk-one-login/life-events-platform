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
class HmppsTopicFactoryTest {

  private val localstackArnPrefix = "arn:aws:sns:eu-west-2:000000000000:"

  private val context = mock<ConfigurableApplicationContext>()
  private val beanFactory = mock<ConfigurableListableBeanFactory>()
  private val snsFactory = mock<AmazonSnsFactory>()
  private val hmppsTopicFactory = HmppsTopicFactory(context, snsFactory)

  init {
    whenever(context.beanFactory).thenReturn(beanFactory)
  }

  @Nested
  inner class `Create AWS HmppsTopic` {
    private val someTopicConfig =
      HmppsSqsProperties.TopicConfig(
        arn = "some arn",
        accessKeyId = "some access key id",
        secretAccessKey = "some secret access key"
      )
    private val hmppsSqsProperties =
      HmppsSqsProperties(queues = mock(), topics = mapOf("sometopicid" to someTopicConfig))
    private val snsClient = mock<AmazonSNS>()
    private lateinit var hmppsTopics: List<HmppsTopic>

    @BeforeEach
    fun `configure mocks and register queues`() {
      whenever(snsFactory.awsSnsClient(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(snsClient)

      hmppsTopics = hmppsTopicFactory.createHmppsTopics(hmppsSqsProperties)
    }

    @Test
    fun `should create aws sns client`() {
      verify(snsFactory).awsSnsClient("sometopicid", "some access key id", "some secret access key", "eu-west-2")
    }

    @Test
    fun `should return the topic details`() {
      assertThat(hmppsTopics[0].id).isEqualTo("sometopicid")
    }

    @Test
    fun `should return the AmazonSNS client`() {
      assertThat(hmppsTopics[0].snsClient).isEqualTo(snsClient)
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
  inner class `Create LocalStack HmppsTopic` {
    private val someTopicConfig = HmppsSqsProperties.TopicConfig(
      arn = "${localstackArnPrefix}some-topic-name",
      accessKeyId = "some access key id",
      secretAccessKey = "some secret access key"
    )
    private val hmppsSqsProperties =
      HmppsSqsProperties(provider = "localstack", queues = mock(), topics = mapOf("sometopicid" to someTopicConfig))
    private val snsClient = mock<AmazonSNS>()
    private lateinit var hmppsTopics: List<HmppsTopic>

    @BeforeEach
    fun `configure mocks and register queues`() {
      whenever(snsFactory.localstackSnsClient(anyString(), anyString(), anyString()))
        .thenReturn(snsClient)

      hmppsTopics = hmppsTopicFactory.createHmppsTopics(hmppsSqsProperties)
    }

    @Test
    fun `should create localstack sns client`() {
      verify(snsFactory).localstackSnsClient("sometopicid", "http://localhost:4566", "eu-west-2")
    }

    @Test
    fun `should return the topic details`() {
      assertThat(hmppsTopics[0].id).isEqualTo("sometopicid")
    }

    @Test
    fun `should return the AmazonSNS client`() {
      assertThat(hmppsTopics[0].snsClient).isEqualTo(snsClient)
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
  inner class `Create multiple AWS HmppsTopics` {
    private val someTopicConfig =
      HmppsSqsProperties.TopicConfig(
        arn = "some arn",
        accessKeyId = "some access key id",
        secretAccessKey = "some secret access key"
      )
    private val anotherTopicConfig = HmppsSqsProperties.TopicConfig(
      arn = "another arn",
      accessKeyId = "another access key id",
      secretAccessKey = "another secret access key"
    )
    private val hmppsSqsProperties = HmppsSqsProperties(
      queues = mock(),
      topics = mapOf("sometopicid" to someTopicConfig, "anothertopicid" to anotherTopicConfig)
    )
    private val snsClient = mock<AmazonSNS>()
    private lateinit var hmppsTopics: List<HmppsTopic>

    @BeforeEach
    fun `configure mocks and register queues`() {
      whenever(snsFactory.awsSnsClient(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(snsClient)
        .thenReturn(snsClient)

      hmppsTopics = hmppsTopicFactory.createHmppsTopics(hmppsSqsProperties)
    }

    @Test
    fun `should create 2 aws sns clients`() {
      verify(snsFactory).awsSnsClient("sometopicid", "some access key id", "some secret access key", "eu-west-2")
      verify(snsFactory).awsSnsClient(
        "anothertopicid",
        "another access key id",
        "another secret access key",
        "eu-west-2"
      )
    }

    @Test
    fun `should return the topic details`() {
      assertThat(hmppsTopics[0].id).isEqualTo("sometopicid")
      assertThat(hmppsTopics[1].id).isEqualTo("anothertopicid")
    }

    @Test
    fun `should return 2 AmazonSNS client`() {
      assertThat(hmppsTopics[0].snsClient).isEqualTo(snsClient)
      assertThat(hmppsTopics[1].snsClient).isEqualTo(snsClient)
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
  inner class `Create multiple LocalStack HmppsTopics` {
    private val someTopicConfig = HmppsSqsProperties.TopicConfig(
      arn = "${localstackArnPrefix}some arn",
      accessKeyId = "some access key id",
      secretAccessKey = "some secret access key"
    )
    private val anotherTopicConfig = HmppsSqsProperties.TopicConfig(
      arn = "${localstackArnPrefix}another arn",
      accessKeyId = "another access key id",
      secretAccessKey = "another secret access key"
    )
    private val hmppsSqsProperties = HmppsSqsProperties(
      provider = "localstack",
      queues = mock(),
      topics = mapOf("sometopicid" to someTopicConfig, "anothertopicid" to anotherTopicConfig)
    )
    private val snsClient = mock<AmazonSNS>()
    private lateinit var hmppsTopics: List<HmppsTopic>

    @BeforeEach
    fun `configure mocks and register queues`() {
      whenever(snsFactory.localstackSnsClient(anyString(), anyString(), anyString()))
        .thenReturn(snsClient)
        .thenReturn(snsClient)

      hmppsTopics = hmppsTopicFactory.createHmppsTopics(hmppsSqsProperties)
    }

    @Test
    fun `should create 2 aws sns clients`() {
      verify(snsFactory).localstackSnsClient("sometopicid", "http://localhost:4566", "eu-west-2")
      verify(snsFactory).localstackSnsClient("anothertopicid", "http://localhost:4566", "eu-west-2")
    }

    @Test
    fun `should return the topic details`() {
      assertThat(hmppsTopics[0].id).isEqualTo("sometopicid")
      assertThat(hmppsTopics[1].id).isEqualTo("anothertopicid")
    }

    @Test
    fun `should return 2 AmazonSNS client`() {
      assertThat(hmppsTopics[0].snsClient).isEqualTo(snsClient)
      assertThat(hmppsTopics[1].snsClient).isEqualTo(snsClient)
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
