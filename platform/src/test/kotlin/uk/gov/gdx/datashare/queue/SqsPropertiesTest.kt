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
            "queueid3" to validAwsQueueConfig(3).copy(queueName = "1stQueueName"),
          ),
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
          queues = mapOf(
            "queueid1" to validAwsQueueConfig(1).copy(dlqName = "1stDlqName"),
            "queueid2" to validAwsQueueConfig(2).copy(dlqName = "2ndDlqName"),
            "queueid3" to validAwsQueueConfig(3).copy(dlqName = "2ndDlqName"),
          ),
        )
      }.isInstanceOf(InvalidAwsSqsPropertiesException::class.java)
        .hasMessageContaining("Found duplicated dlq name")
        .hasMessageContaining("2ndDlqName")
        .hasMessageNotContaining("1stDlqName")
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
            "queueid1" to validLocalStackQueueConfig(1).copy(queueName = "1stQueueName"),
            "queueid2" to validLocalStackQueueConfig(2).copy(queueName = "2ndQueueName"),
            "queueid3" to validLocalStackQueueConfig(3).copy(queueName = "1stQueueName"),
          ),
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
            "queueid1" to validLocalStackQueueConfig(1).copy(dlqName = "1stDlqName"),
            "queueid2" to validLocalStackQueueConfig(2).copy(dlqName = "2ndDlqName"),
            "queueid3" to validLocalStackQueueConfig(3).copy(dlqName = "2ndDlqName"),
          ),
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
            "queueid1" to validLocalStackQueueNoDlqConfig(1),
            "queueid2" to validLocalStackQueueNoDlqConfig(2),
            "queueid3" to validLocalStackQueueNoDlqConfig(3),
          ),
        )
      }
    }
  }

  private fun validAwsQueueConfig(index: Int = 1) = SqsProperties.QueueConfig(
    queueName = "name$index",
    dlqName = "dlqName$index",
  )

  private fun validLocalStackQueueConfig(index: Int = 1) =
    SqsProperties.QueueConfig(queueName = "name$index", dlqName = "dlqName$index")

  private fun validLocalStackQueueNoDlqConfig(index: Int = 1) = SqsProperties.QueueConfig(queueName = "name$index")
}
