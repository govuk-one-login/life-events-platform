package uk.gov.gdx.datashare.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.cloudwatch.model.ComparisonOperator
import software.amazon.awssdk.services.cloudwatch.model.DeleteAlarmsRequest
import software.amazon.awssdk.services.cloudwatch.model.Dimension
import software.amazon.awssdk.services.cloudwatch.model.PutMetricAlarmRequest

@Service
class CloudWatchService(
  @Value("\${environment}") private val environment: String,
  @Value("\${alert-sns-topic-arn:#{null}}") private val topicArn: String?,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  private val cloudWatchClient by lazy { CloudWatchClient.create() }

  fun createSqsAlarm(queueName: String) {
    val threshold = 10000.0
    val putAlarmRequest = PutMetricAlarmRequest.builder()
      .namespace("AWS/SDK")
      .metricName("ApproximateNumberOfMessagesVisible")
      .dimensions(Dimension.builder().name("QueueName").value(queueName).build())
      .alarmName("$environment-unconsumed-messages-$queueName")
      .alarmDescription("Over $threshold messages visible on queue $queueName")
      .threshold(threshold)
      .evaluationPeriods(2)
      .treatMissingData("notBreaching")
      .comparisonOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL_TO_THRESHOLD)
      .alarmActions(topicArn)
      .okActions(topicArn)
      .build()

    cloudWatchClient.putMetricAlarm(putAlarmRequest)
  }

  fun deleteSqsAlarm(queueName: String) {
    val deleteRequest = DeleteAlarmsRequest
      .builder()
      .alarmNames("$environment-unconsumed-messages-$queueName")
      .build()

    cloudWatchClient.deleteAlarms(deleteRequest)
  }
}
