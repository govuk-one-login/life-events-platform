package uk.gov.gdx.datashare.queue

import com.amazonaws.services.sns.AmazonSNS
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ConfigurableApplicationContext

class HmppsTopicFactory(
  private val context: ConfigurableApplicationContext,
  private val amazonSnsFactory: AmazonSnsFactory,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun createHmppsTopics(hmppsSqsProperties: HmppsSqsProperties) =
    hmppsSqsProperties.topics
      .map { (topicId, topicConfig) ->
        val snsClient = getOrDefaultSnsClient(topicId, topicConfig, hmppsSqsProperties)
        HmppsTopic(topicId, topicConfig.arn, snsClient)
          .also { getOrDefaultHealthIndicator(it) }
      }.toList()

  private fun getOrDefaultHealthIndicator(topic: HmppsTopic) {
    "${topic.id}-health".let { beanName ->
      runCatching { context.beanFactory.getBean(beanName) as AmazonSNS }
        .getOrElse {
          HmppsTopicHealth(topic)
            .also { context.beanFactory.registerSingleton(beanName, it) }
        }
    }
  }

  private fun getOrDefaultSnsClient(topicId: String, topicConfig: HmppsSqsProperties.TopicConfig, hmppsSqsProperties: HmppsSqsProperties): AmazonSNS =
    "$topicId-sns-client".let { beanName ->
      runCatching { context.beanFactory.getBean(beanName) as AmazonSNS }
        .getOrElse {
          createSnsClient(topicId, topicConfig, hmppsSqsProperties)
            .also { context.beanFactory.registerSingleton(beanName, it) }
        }
    }

  fun createSnsClient(topicId: String, topicConfig: HmppsSqsProperties.TopicConfig, hmppsSqsProperties: HmppsSqsProperties) =
    with(hmppsSqsProperties) {
      when (provider) {
        "aws" -> amazonSnsFactory.awsSnsClient(topicId, topicConfig.accessKeyId, topicConfig.secretAccessKey, region, topicConfig.asyncClient)
        "localstack" -> amazonSnsFactory.localstackSnsClient(topicId, localstackUrl, region, topicConfig.asyncClient)
          .also { it.createTopic(topicConfig.name) }
          .also { log.info("Created a LocalStack SNS topic for topicId $topicId with ARN ${topicConfig.arn}") }
        else -> throw IllegalStateException("Unrecognised HMPPS SQS provider $provider")
      }
    }
}
