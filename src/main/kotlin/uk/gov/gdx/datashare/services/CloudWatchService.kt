package uk.gov.gdx.datashare.services

import org.springframework.stereotype.Service
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.cloudwatch.model.Dimension
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataRequest
import software.amazon.awssdk.services.cloudwatch.model.Metric
import software.amazon.awssdk.services.cloudwatch.model.MetricDataQuery
import software.amazon.awssdk.services.cloudwatch.model.MetricStat
import java.time.Instant

@Service
class CloudWatchService {
  private val cloudWatchClient by lazy { CloudWatchClient.create() }

  fun getQueuesAgeOfOldestMessage(queueNames: List<String>): Map<String, Double?> {
    val getMetricDataRequest = GetMetricDataRequest.builder()
      .metricDataQueries(
        queueNames.map {
          MetricDataQuery.builder()
            .metricStat(
              MetricStat.builder()
                .metric(
                  Metric.builder()
                    .namespace("AWS/SQS")
                    .metricName("ApproximateAgeOfOldestMessage")
                    .dimensions(Dimension.builder().name("QueueName").value(it).build())
                    .build(),
                )
                .period(60)
                .stat("Maximum")
                .build(),
            )
            .id(it)
            .build()
        },
      )
      .startTime(Instant.now().minusSeconds(120))
      .endTime(Instant.now())
      .build()

    val metricDataResults = cloudWatchClient.getMetricData(getMetricDataRequest).metricDataResults()
    return metricDataResults.associate { it.id() to it.values().firstOrNull() }
  }
}
