package uk.gov.gdx.datashare.config

import io.micrometer.cloudwatch2.CloudWatchConfig
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.logging.LoggingMeterRegistry
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient
import java.time.Duration

@Configuration
class MicrometerConfiguration {

  @Bean
  fun getMeterRegistry(
    @Value("\${metrics.cloudwatch.namespace}") namespace: String,
    @Value("\${metrics.log-to-console:#{false}}") logToConsole: Boolean,
  ): MeterRegistry? {
    if (logToConsole) {
      return LoggingMeterRegistry()
    }
    val cloudWatchConfig: CloudWatchConfig = setupCloudWatchConfig(namespace)
    return CloudWatchMeterRegistry(
      cloudWatchConfig,
      Clock.SYSTEM,
      cloudWatchAsyncClient(),
    )
  }

  private fun setupCloudWatchConfig(namespace: String): CloudWatchConfig {
    val cloudWatchConfig = object : CloudWatchConfig {
      private val configuration = mapOf(
        "cloudwatch.namespace" to namespace,
        "cloudwatch.step" to Duration.ofMinutes(1).toString(),
      )

      override fun get(key: String): String? {
        return configuration[key]
      }
    }
    return cloudWatchConfig
  }

  private fun cloudWatchAsyncClient(): CloudWatchAsyncClient {
    return CloudWatchAsyncClient
      .builder()
      .region(Region.EU_WEST_2)
      .build()
  }
}
