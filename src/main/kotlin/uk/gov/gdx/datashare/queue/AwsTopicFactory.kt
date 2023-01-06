package uk.gov.gdx.datashare.queue

import com.amazonaws.services.sns.AmazonSNS
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ConfigurableApplicationContext

class AwsTopicFactory(
  private val context: ConfigurableApplicationContext,
  private val amazonSnsFactory: AmazonSnsFactory,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun createAwsTopics(sqsProperties: SqsProperties) =
    sqsProperties.topics
      .map { (topicId, topicConfig) ->
        val snsClient = getOrDefaultSnsClient(topicId, topicConfig, sqsProperties)
        AwsTopic(topicId, topicConfig.arn, snsClient)
          .also { getOrDefaultHealthIndicator(it) }
      }.toList()

  private fun getOrDefaultHealthIndicator(topic: AwsTopic) {
    "${topic.id}-health".let { beanName ->
      runCatching { context.beanFactory.getBean(beanName) as AmazonSNS }
        .getOrElse {
          AwsTopicHealth(topic)
            .also { context.beanFactory.registerSingleton(beanName, it) }
        }
    }
  }

  private fun getOrDefaultSnsClient(topicId: String, topicConfig: SqsProperties.TopicConfig, sqsProperties: SqsProperties): AmazonSNS =
    "$topicId-sns-client".let { beanName ->
      runCatching { context.beanFactory.getBean(beanName) as AmazonSNS }
        .getOrElse {
          createSnsClient(topicId, topicConfig, sqsProperties)
            .also { context.beanFactory.registerSingleton(beanName, it) }
        }
    }

  fun createSnsClient(topicId: String, topicConfig: SqsProperties.TopicConfig, sqsProperties: SqsProperties) =
    with(sqsProperties) {
      when (provider) {
        "aws" -> amazonSnsFactory.awsSnsClient(topicId, topicConfig.accessKeyId, topicConfig.secretAccessKey, region)
        "localstack" -> amazonSnsFactory.localstackSnsClient(topicId, localstackUrl, region)
          .also { it.createTopic(topicConfig.name) }
          .also { log.info("Created a LocalStack SNS topic for topicId $topicId with ARN ${topicConfig.arn}") }

        else -> throw IllegalStateException("Unrecognised SQS provider $provider")
      }
    }
}
