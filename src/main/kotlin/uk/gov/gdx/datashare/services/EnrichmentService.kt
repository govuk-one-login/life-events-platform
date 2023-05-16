package uk.gov.gdx.datashare.services

import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.models.EventDetails

interface EnrichmentService {
  fun accepts(eventType: EventType): Boolean
  fun process(
    dataId: String,
    enrichmentFields: List<EnrichmentField>,
  ): EventDetails?
}
