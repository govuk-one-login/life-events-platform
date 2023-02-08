package uk.gov.gdx.datashare.services

import com.amazonaws.xray.spring.aop.XRayEnabled
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.models.TestEvent

@Service
@XRayEnabled
class TestEventEnrichmentService : EnrichmentService {
  override fun accepts(eventType: EventType): Boolean {
    return eventType == EventType.TEST_EVENT;
  }

  override fun process(eventType: EventType, dataId: String, enrichmentFields: List<EnrichmentField>): Any? {
    return TestEvent(testField = "Test Field Value")
  }
}
