package uk.gov.gdx.datashare.services

import com.amazonaws.xray.spring.aop.XRayEnabled
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.models.EventDetails
import uk.gov.gdx.datashare.models.TestEventDetails

@Service
@XRayEnabled
class TestEventEnrichmentService : EnrichmentService {
  override fun accepts(eventType: EventType): Boolean {
    return eventType == EventType.TEST_EVENT
  }

  override fun process(eventType: EventType, dataId: String, enrichmentFields: List<EnrichmentField>): EventDetails? {
    return TestEventDetails(testField = "Test Field Value")
  }
}
