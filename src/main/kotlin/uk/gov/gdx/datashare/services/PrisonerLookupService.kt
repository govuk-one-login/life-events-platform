package uk.gov.gdx.datashare.services

import com.amazonaws.xray.spring.aop.XRayEnabled
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.models.PrisonerDetails

@Service
@XRayEnabled
@ConditionalOnProperty(name = ["api.base.prisoner-event.enabled"], havingValue = "true")
class PrisonerLookupService(
  private val prisonerApiService: PrisonerApiService,
) : EnrichmentService {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  override fun accepts(eventType: EventType): Boolean {
    return eventType == EventType.ENTERED_PRISON
  }

  override fun process(
    dataId: String,
    enrichmentFields: List<EnrichmentField>,
  ): PrisonerDetails? {
    val allEnrichedData = prisonerApiService.findPrisonerById(dataId)

    return allEnrichedData?.let { PrisonerDetails.from(enrichmentFields, it) }
  }
}
