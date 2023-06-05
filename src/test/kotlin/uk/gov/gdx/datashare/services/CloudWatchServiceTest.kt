package uk.gov.gdx.datashare.uk.gov.gdx.datashare.services

import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.cloudwatch.model.*
import uk.gov.gdx.datashare.services.CloudWatchService

class CloudWatchServiceTest {
  private val environment = "test"
  private val topicArn = "topic:arn"

  private val underTest = CloudWatchService(environment, topicArn)

  private val cloudwatchClient = mockk<CloudWatchClient>()

  private val queueName = "queue"

  @BeforeEach
  fun setup() {
    mockkStatic(CloudWatchClient::class)
    every { CloudWatchClient.create() } returns cloudwatchClient
  }

  @Test
  fun `createSqsAlarm creates alarm`() {
    every { cloudwatchClient.putMetricAlarm(any<PutMetricAlarmRequest>()) } returns mockk<PutMetricAlarmResponse>()

    underTest.createSqsAlarm(queueName)

    verify(exactly = 1) {
      cloudwatchClient.putMetricAlarm(withArg<PutMetricAlarmRequest> {
        assertThat(it.namespace()).isEqualTo("AWS/SDK")
        assertThat(it.metricName()).isEqualTo("ApproximateNumberOfMessagesVisible")
        assertThat(it.dimensions()).hasSize(1)
        assertThat(it.dimensions().first().name()).isEqualTo("QueueName")
        assertThat(it.dimensions().first().value()).isEqualTo(queueName)
        assertThat(it.alarmName()).isEqualTo("$environment-unconsumed-messages-$queueName")
        assertThat(it.alarmDescription()).isEqualTo("Over 10000.0 messages visible on queue $queueName")
        assertThat(it.threshold()).isEqualTo(10000.0)
        assertThat(it.evaluationPeriods()).isEqualTo(2)
        assertThat(it.treatMissingData()).isEqualTo("notBreaching")
        assertThat(it.comparisonOperator()).isEqualTo(ComparisonOperator.GREATER_THAN_OR_EQUAL_TO_THRESHOLD)
        assertThat(it.alarmActions()).isEqualTo(listOf(topicArn))
        assertThat(it.okActions()).isEqualTo(listOf(topicArn))
      })
    }
  }

  @Test
  fun `deleteSqsAlarm deletes alarm`() {
    every { cloudwatchClient.deleteAlarms(any<DeleteAlarmsRequest>()) } returns mockk<DeleteAlarmsResponse>()

    underTest.deleteSqsAlarm(queueName)

    verify(exactly = 1) {
      cloudwatchClient.deleteAlarms(withArg<DeleteAlarmsRequest> {
        assertThat(it.alarmNames()).isEqualTo(listOf("$environment-unconsumed-messages-$queueName"))
      })
    }
  }
}
