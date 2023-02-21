package uk.gov.gdx.datashare.config

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MicrometerConfiguration(
  private val taskId: String,
) {
  @Bean
  fun metricsCommonTags(): MeterRegistryCustomizer<MeterRegistry> = MeterRegistryCustomizer { registry ->
    registry.config().commonTags("task.id", taskId)
  }
}
