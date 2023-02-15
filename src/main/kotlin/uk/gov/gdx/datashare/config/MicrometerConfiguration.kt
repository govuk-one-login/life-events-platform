package uk.gov.gdx.datashare.config

import io.micrometer.cloudwatch2.CloudWatchConfig
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry
import io.micrometer.core.aop.CountedAspect
import io.micrometer.core.aop.TimedAspect
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.logging.LoggingMeterRegistry
import io.micrometer.core.instrument.logging.LoggingRegistryConfig
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient
import java.time.Duration

@Configuration
class MicrometerConfiguration {
  companion object {
    private val log = LoggerFactory.getLogger(LoggingMeterRegistry::class.java)
  }

  @Bean
  fun getMeterRegistry(
    @Value("\${metrics.cloudwatch.namespace}") namespace: String,
    @Value("\${metrics.log-to-console:#{false}}") logToConsole: Boolean,
  ): MeterRegistry =
    if (logToConsole) {
      LoggingMeterRegistry.builder(LoggingRegistryConfig.DEFAULT).loggingSink(log::debug).build()
    } else {
      CloudWatchMeterRegistry(
        setupCloudWatchConfig(namespace),
        Clock.SYSTEM,
        cloudWatchAsyncClient(),
      )
    }

  @Bean
  fun timedAspect(registry: MeterRegistry) = TimedAspect(registry)

  @Bean
  fun countedAspect(registry: MeterRegistry) = CountedAspect(registry)

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

  private fun cloudWatchAsyncClient() = CloudWatchAsyncClient
    .builder()
    .region(Region.EU_WEST_2)
    .build()
}
