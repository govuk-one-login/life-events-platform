package uk.gov.gdx.datashare.services

import com.amazonaws.xray.spring.aop.XRayEnabled
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType

@Service
@Order(value = Ordered.LOWEST_PRECEDENCE)
@XRayEnabled
class NullEventEnrichmentService : EnrichmentService {
  override fun accepts(eventType: EventType): Boolean = true

  override fun process(eventType: EventType, dataId: String, enrichmentFields: List<EnrichmentField>): Any? {
    log.warn("Not handling this event type {}", eventType)
    return null
  }

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}
