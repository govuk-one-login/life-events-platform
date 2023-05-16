package uk.gov.gdx.datashare.services

import com.amazonaws.xray.spring.aop.XRayEnabled
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.models.GroDeathNotificationDetails

@Service
@XRayEnabled
class GroDeathNotificationService(
  private val groApiService: GroApiService,
) : EnrichmentService {
  override fun accepts(eventType: EventType): Boolean {
    return eventType == EventType.GRO_DEATH_NOTIFICATION
  }

  override fun process(
    dataId: String,
    enrichmentFields: List<EnrichmentField>,
  ): GroDeathNotificationDetails {
    return groApiService
      .enrichEvent(dataId)
      .let { GroDeathNotificationDetails.from(enrichmentFields, it) }
  }
}
