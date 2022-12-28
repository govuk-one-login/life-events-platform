package uk.gov.gdx.datashare.config

import io.micrometer.cloudwatch2.CloudWatchConfig
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient
import java.time.Duration


@Configuration
class MicrometerConfiguration {

  @Bean
  fun cloudWatchAsyncClient(): CloudWatchAsyncClient {
    return CloudWatchAsyncClient
      .builder()
      .region(Region.EU_WEST_2)
      .build()
  }

  @Bean
  fun getMeterRegistry(): MeterRegistry? {
    val cloudWatchConfig: CloudWatchConfig = setupCloudWatchConfig()
    return CloudWatchMeterRegistry(
      cloudWatchConfig,
      Clock.SYSTEM,
      cloudWatchAsyncClient()
    )
  }

  private fun setupCloudWatchConfig(): CloudWatchConfig {
    val cloudWatchConfig = object : CloudWatchConfig {
      private val configuration = mapOf(
        "cloudwatch.step" to Duration.ofMinutes(1).toString()
      )
      override fun get(key: String): String? {
        return configuration[key]
      }
    }
    return cloudWatchConfig
  }

}