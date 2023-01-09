package uk.gov.gdx.datashare.queue

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SqsPropertiesTest {

  private val localstackArnPrefix = "arn:aws:sns:eu-west-2:000000000000:"

  @Nested
  inner class GeneralRules {

    @Test
    fun `should not allow lowercase queueId`() {
      assertThatThrownBy {
        SqsProperties(queues = mapOf("notLowerCaseQueueId" to validAwsQueueConfig()))
      }.isInstanceOf(InvalidAwsSqsPropertiesException::class.java)
        .hasMessageContaining("notLowerCaseQueueId")
        .hasMessageContaining("lowercase")
    }

    @Test
    fun `should not allow lowercase topicId`() {
      assertThatThrownBy {
        SqsProperties(
          queues = mapOf("queueid" to validAwsQueueConfig()),
          topics = mapOf("notLowerCaseTopicId" to validAwsTopicConfig())
        )
      }.isInstanceOf(InvalidAwsSqsPropertiesException::class.java)
        .hasMessageContaining("notLowerCaseTopicId")
        .hasMessageContaining("lowercase")
    }

    @Test
    fun `should retrieve name for localstack topic`() {
      val properties = SqsProperties(
        provider = "localstack",
        queues = mapOf("queueid" to validLocalstackQueueConfig()),
        topics = mapOf("topicid" to validLocalstackTopicConfig().copy(arn = "${localstackArnPrefix}topic-name"))
      )

      assertThat(properties.topics["topicid"]?.name).isEqualTo("topic-name")
    }
  }

  @Nested
  inner class AwsMandatoryProperties {
    @Test
    fun `should require a queue access key ID`() {
      assertThatThrownBy {
        SqsProperties(queues = mapOf("queueid" to validAwsQueueConfig().copy(queueAccessKeyId = "")))
      }.isInstanceOf(InvalidAwsSqsPropertiesException::class.java)
        .hasMessageContaining("queueid")
        .hasMessageContaining("queue access key id")
    }

    @Test
    fun `should require a queue secret access key`() {
      assertThatThrownBy {
        SqsProperties(queues = mapOf("queueid" to validAwsQueueConfig().copy(queueSecretAccessKey = "")))
      }.isInstanceOf(InvalidAwsSqsPropertiesException::class.java)
        .hasMessageContaining("queueid")
        .hasMessageContaining("queue secret access key")
    }

    @Test
    fun `should require a dlq access key ID`() {
      assertThatThrownBy {
        SqsProperties(queues = mapOf("queueid" to validAwsQueueConfig().copy(dlqAccessKeyId = "")))
      }.isInstanceOf(InvalidAwsSqsPropertiesException::class.java)
        .hasMessageContaining("queueid")
        .hasMessageContaining("DLQ access key id")
    }

    @Test
    fun `should not require a dlq access key ID if no dlq exists`() {
      assertThatNoException().isThrownBy {
        SqsProperties(queues = mapOf("queueid" to validAwsQueueNoDlqConfig().copy(dlqAccessKeyId = "")))
      }
    }

    @Test
    fun `should require a dlq secret access key`() {
      assertThatThrownBy {
        SqsProperties(queues = mapOf("queueid" to validAwsQueueConfig().copy(dlqSecretAccessKey = "")))
      }.isInstanceOf(InvalidAwsSqsPropertiesException::class.java)
        .hasMessageContaining("queueid")
        .hasMessageContaining("DLQ secret access key")
    }

    @Test
    fun `should not require a dlq secret access key if no dlq exists`() {
      assertThatNoException().isThrownBy {
        SqsProperties(queues = mapOf("queueid" to validAwsQueueNoDlqConfig().copy(dlqSecretAccessKey = "")))
      }
    }

    @Test
    fun `topics should have an arn`() {
      assertThatThrownBy {
        SqsProperties(
          queues = mapOf("queueid" to validAwsQueueConfig()),
          topics = mapOf("topicid" to validAwsTopicConfig().copy(arn = ""))
        )
      }.isInstanceOf(InvalidAwsSqsPropertiesException::class.java)
        .hasMessageContaining("topicid")
        .hasMessageContaining("arn")
    }

    @Test
    fun `topics should have an access key id`() {
      assertThatThrownBy {
        SqsProperties(
          queues = mapOf("queueid" to validAwsQueueConfig()),
          topics = mapOf("topicid" to validAwsTopicConfig().copy(accessKeyId = ""))
        )
      }.isInstanceOf(InvalidAwsSqsPropertiesException::class.java)
        .hasMessageContaining("topicid")
        .hasMessageContaining("access key id")
    }

    @Test
    fun `topics should have a secret access key`() {
      assertThatThrownBy {
        SqsProperties(
          queues = mapOf("queueid" to validAwsQueueConfig()),
          topics = mapOf("topicid" to validAwsTopicConfig().copy(secretAccessKey = ""))
        )
      }.isInstanceOf(InvalidAwsSqsPropertiesException::class.java)
        .hasMessageContaining("topicid")
        .hasMessageContaining("secret access key")
    }
  }

  @Nested
  inner class LocalStackMandatoryProperties {

    @Test
    fun `topic name should exist`() {
      assertThatThrownBy {
        SqsProperties(
          provider = "localstack",
          queues = mapOf("queueid" to validLocalstackQueueConfig()),
          topics = mapOf("topicid" to validLocalstackTopicConfig().copy(arn = localstackArnPrefix))
        )
      }.isInstanceOf(InvalidAwsSqsPropertiesException::class.java)
        .hasMessageContaining("topicid")
        .hasMessageContaining("name")
    }

    @Test
    fun `topic should exist if subscribing to it`() {
      assertThatThrownBy {
        SqsProperties(
          provider = "localstack",
          queues = mapOf("queueid" to validLocalstackQueueConfig().copy(subscribeTopicId = "topicid"))
        )
      }.isInstanceOf(InvalidAwsSqsPropertiesException::class.java)
        .hasMessageContaining("queueid")
        .hasMessageContaining("topicid")
        .hasMessageContaining("does not exist")
    }
  }

  @Nested
  inner class AwsDuplicateValues {

    @Test
    fun `queue names should be unique`() {
      assertThatThrownBy {
        SqsProperties(
          queues = mapOf(
            "queueid1" to validAwsQueueConfig(1).copy(queueName = "1stQueueName"),
            "queueid2" to validAwsQueueConfig(2).copy(queueName = "2ndQueueName"),
            "queueid3" to validAwsQueueConfig(3).copy(queueName = "1stQueueName")
          ),
          topics = mapOf()
        )
      }.isInstanceOf(InvalidAwsSqsPropertiesException::class.java)
        .hasMessageContaining("Found duplicated queue name")
        .hasMessageContaining("1stQueueName")
        .hasMessageNotContaining("2ndQueueName")
    }

    @Test
    fun `access key ids should be unique`() {
      assertThatThrownBy {
        SqsProperties(
          queues = mapOf(
            "queueid1" to validAwsQueueConfig(1).copy(queueAccessKeyId = "1stAccessKey"),
            "queueid2" to validAwsQueueConfig(2).copy(queueAccessKeyId = "2ndAccessKey"),
            "queueid3" to validAwsQueueConfig(3).copy(queueAccessKeyId = "1stAccessKey"),
            "queueid4" to validAwsQueueConfig(4).copy(queueAccessKeyId = "2ndAccessKey"),
            "queueid5" to validAwsQueueConfig(5).copy(queueAccessKeyId = "3rdAccessKey")
          ),
          topics = mapOf()
        )
      }.isInstanceOf(InvalidAwsSqsPropertiesException::class.java)
        .hasMessageContaining("Found duplicated queue access key id")
        .hasMessageContaining("1stA******")
        .hasMessageContaining("2ndA******")
        .hasMessageNotContaining("3rdA******")
    }

    @Test
    fun `queue secret access keys should be unique`() {
      assertThatThrownBy {
        SqsProperties(
          queues = mapOf(
            "queueid1" to validAwsQueueConfig(1).copy(queueSecretAccessKey = "1stSecretKey"),
            "queueid2" to validAwsQueueConfig(2).copy(queueSecretAccessKey = "1stSecretKey"),
            "queueid3" to validAwsQueueConfig(3).copy(queueSecretAccessKey = "2ndSecretKey")
          ),
          topics = mapOf()
        )
      }.isInstanceOf(InvalidAwsSqsPropertiesException::class.java)
        .hasMessageContaining("Found duplicated queue secret access keys")
        .hasMessageContaining("1stS******")
        .hasMessageNotContaining("2ndS******")
    }

    @Test
    fun `dlq names should be unique`() {
      assertThatThrownBy {
        SqsProperties(
          queues = mapOf(
            "queueid1" to validAwsQueueConfig(1).copy(dlqName = "1stDlqName"),
            "queueid2" to validAwsQueueConfig(2).copy(dlqName = "2ndDlqName"),
            "queueid3" to validAwsQueueConfig(3).copy(dlqName = "2ndDlqName")
          ),
          topics = mapOf()
        )
      }.isInstanceOf(InvalidAwsSqsPropertiesException::class.java)
        .hasMessageContaining("Found duplicated dlq name")
        .hasMessageContaining("2ndDlqName")
        .hasMessageNotContaining("1stDlqName")
    }

    @Test
    fun `dlq access key ids should be unique`() {
      assertThatThrownBy {
        SqsProperties(
          queues = mapOf(
            "queueid1" to validAwsQueueConfig(1).copy(dlqAccessKeyId = "1stAccessKey"),
            "queueid2" to validAwsQueueConfig(2).copy(dlqAccessKeyId = "2ndAccessKey"),
            "queueid3" to validAwsQueueConfig(3).copy(dlqAccessKeyId = "1stAccessKey"),
          ),
          topics = mapOf()
        )
      }.isInstanceOf(InvalidAwsSqsPropertiesException::class.java)
        .hasMessageContaining("Found duplicated dlq access key id")
        .hasMessageContaining("1stA******")
        .hasMessageNotContaining("2ndA******")
    }

    @Test
    fun `dlq secret access keys should be unique`() {
      assertThatThrownBy {
        SqsProperties(
          queues = mapOf(
            "queueid1" to validAwsQueueConfig(1).copy(dlqSecretAccessKey = "1stSecretKey"),
            "queueid2" to validAwsQueueConfig(2).copy(dlqSecretAccessKey = "1stSecretKey"),
            "queueid3" to validAwsQueueConfig(3).copy(dlqSecretAccessKey = "2ndSecretKey")
          ),
          topics = mapOf()
        )
      }.isInstanceOf(InvalidAwsSqsPropertiesException::class.java)
        .hasMessageContaining("Found duplicated dlq secret access keys")
        .hasMessageContaining("1stS******")
        .hasMessageNotContaining("2ndS******")
    }

    @Test
    fun `topic arns should be unique`() {
      assertThatThrownBy {
        SqsProperties(
          queues = mapOf(),
          topics = mapOf(
            "topic1" to validAwsTopicConfig(1).copy(arn = "1stArn"),
            "topic2" to validAwsTopicConfig(2).copy(arn = "2ndArn"),
            "topic3" to validAwsTopicConfig(3).copy(arn = "1stArn")
          )
        )
      }.isInstanceOf(InvalidAwsSqsPropertiesException::class.java)
        .hasMessageContaining("Found duplicated topic arns")
        .hasMessageContaining("1stArn")
        .hasMessageNotContaining("2ndArn")
    }

    @Test
    fun `topic access key ids should be unique`() {
      assertThatThrownBy {
        SqsProperties(
          queues = mapOf(),
          topics = mapOf(
            "topic1" to validAwsTopicConfig(1).copy(accessKeyId = "1stAccessKey"),
            "topic2" to validAwsTopicConfig(2).copy(accessKeyId = "2ndAccessKey"),
            "topic3" to validAwsTopicConfig(3).copy(accessKeyId = "1stAccessKey")
          )
        )
      }.isInstanceOf(InvalidAwsSqsPropertiesException::class.java)
        .hasMessageContaining("Found duplicated topic access key ids")
        .hasMessageContaining("1stA******")
        .hasMessageNotContaining("2ndA******")
    }

    @Test
    fun `topic secret access keys should be unique`() {
      assertThatThrownBy {
        SqsProperties(
          queues = mapOf(),
          topics = mapOf(
            "topic1" to validAwsTopicConfig(1).copy(secretAccessKey = "1stSecretKey"),
            "topic2" to validAwsTopicConfig(2).copy(secretAccessKey = "2ndSecretKey"),
            "topic3" to validAwsTopicConfig(3).copy(secretAccessKey = "1stSecretKey")
          )
        )
      }.isInstanceOf(InvalidAwsSqsPropertiesException::class.java)
        .hasMessageContaining("Found duplicated topic secret access keys")
        .hasMessageContaining("1stS******")
        .hasMessageNotContaining("2ndS******")
    }
  }

  @Nested
  inner class LocalStackDuplicateValues {

    @Test
    fun `queue names should be unique`() {
      assertThatThrownBy {
        SqsProperties(
          provider = "localstack",
          queues = mapOf(
            "queueid1" to validLocalstackQueueConfig(1).copy(queueName = "1stQueueName"),
            "queueid2" to validLocalstackQueueConfig(2).copy(queueName = "2ndQueueName"),
            "queueid3" to validLocalstackQueueConfig(3).copy(queueName = "1stQueueName")
          ),
          topics = mapOf()
        )
      }.isInstanceOf(InvalidAwsSqsPropertiesException::class.java)
        .hasMessageContaining("Found duplicated queue name")
        .hasMessageContaining("1stQueueName")
        .hasMessageNotContaining("2ndQueueName")
    }

    @Test
    fun `dlq names should be unique`() {
      assertThatThrownBy {
        SqsProperties(
          provider = "localstack",
          queues = mapOf(
            "queueid1" to validLocalstackQueueConfig(1).copy(dlqName = "1stDlqName"),
            "queueid2" to validLocalstackQueueConfig(2).copy(dlqName = "2ndDlqName"),
            "queueid3" to validLocalstackQueueConfig(3).copy(dlqName = "2ndDlqName")
          ),
          topics = mapOf()
        )
      }.isInstanceOf(InvalidAwsSqsPropertiesException::class.java)
        .hasMessageContaining("Found duplicated dlq name")
        .hasMessageContaining("2ndDlqName")
        .hasMessageNotContaining("1stDlqName")
    }

    @Test
    fun `dlq is optional`() {
      assertThatNoException().isThrownBy {
        SqsProperties(
          provider = "localstack",
          queues = mapOf(
            "queueid1" to validLocalstackQueueNoDlqConfig(1),
            "queueid2" to validLocalstackQueueNoDlqConfig(2),
            "queueid3" to validLocalstackQueueNoDlqConfig(3)
          ),
          topics = mapOf()
        )
      }
    }

    @Test
    fun `topic names should be unique`() {
      assertThatThrownBy {
        SqsProperties(
          provider = "localstack",
          queues = mapOf(),
          topics = mapOf(
            "topic1" to validLocalstackTopicConfig(1).copy(arn = "${localstackArnPrefix}1stName"),
            "topic2" to validLocalstackTopicConfig(2).copy(arn = "${localstackArnPrefix}2ndName"),
            "topic3" to validLocalstackTopicConfig(3).copy(arn = "${localstackArnPrefix}1stName")
          )
        )
      }.isInstanceOf(InvalidAwsSqsPropertiesException::class.java)
        .hasMessageContaining("Found duplicated topic names")
        .hasMessageContaining("1stName")
        .hasMessageNotContaining("2ndName")
    }
  }

  @Nested
  inner class TopicArnRegex {

    @Test
    fun `should retrieve name from valid topic arn`() {
      val topicConfig = SqsProperties.TopicConfig(arn = "${localstackArnPrefix}some_topic_name")
      val sqsProperties =
        SqsProperties(provider = "localstack", queues = mapOf(), topics = mapOf("sometopicid" to topicConfig))

      assertThat(sqsProperties.topics["sometopicid"]?.name).isEqualTo("some_topic_name")
    }

    @Test
    fun `should throw if topic arn has invalid format`() {
      val topicConfig = SqsProperties.TopicConfig(arn = "invalid_topic_name")
      assertThatThrownBy {
        SqsProperties(provider = "localstack", queues = mapOf(), topics = mapOf("sometopicid" to topicConfig))
      }.isInstanceOf(InvalidAwsSqsPropertiesException::class.java)
        .hasMessageContaining("invalid_topic_name")
        .hasMessageContaining("invalid format")
    }
  }

  private fun validAwsQueueConfig(index: Int = 1) = SqsProperties.QueueConfig(
    queueName = "name$index",
    queueAccessKeyId = "key$index",
    queueSecretAccessKey = "secret$index",
    dlqName = "dlqName$index",
    dlqAccessKeyId = "dlqKey$index",
    dlqSecretAccessKey = "dlqSecret$index"
  )

  private fun validAwsQueueNoDlqConfig(index: Int = 1) = SqsProperties.QueueConfig(
    queueName = "name$index",
    queueAccessKeyId = "key$index",
    queueSecretAccessKey = "secret$index"
  )

  private fun validAwsTopicConfig(index: Int = 1) =
    SqsProperties.TopicConfig(arn = "arn$index", accessKeyId = "key$index", secretAccessKey = "secret$index")

  private fun validLocalstackQueueConfig(index: Int = 1) =
    SqsProperties.QueueConfig(queueName = "name$index", dlqName = "dlqName$index")

  private fun validLocalstackQueueNoDlqConfig(index: Int = 1) = SqsProperties.QueueConfig(queueName = "name$index")
  private fun validLocalstackTopicConfig(index: Int = 1) =
    SqsProperties.TopicConfig(arn = "${localstackArnPrefix}$index")
}
