package uk.gov.gdx.datashare.config

import com.amazonaws.xray.AWSXRay
import com.amazonaws.xray.AWSXRayRecorderBuilder
import com.amazonaws.xray.javax.servlet.AWSXRayServletFilter
import com.amazonaws.xray.slf4j.SLF4JSegmentListener
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.servlet.Filter

@Configuration
class XRayConfig {
  @Bean
  @ConditionalOnProperty(prefix = "xray-tracing", name=["enabled"], havingValue = "true")
  fun TracingFilter(
    @Value("\${environment}") environment: String,
    @Value("\${spring.application.name}") applicationName: String,
  ): Filter {
    return AWSXRayServletFilter("$environment-$applicationName")
  }

  companion object {
    init {
      val builder: AWSXRayRecorderBuilder = AWSXRayRecorderBuilder.standard()
        .withDefaultPlugins()
        .withFastIdGenerator()
        .withSegmentListener(SLF4JSegmentListener())
      AWSXRay.setGlobalRecorder(builder.build())
    }
  }
}
