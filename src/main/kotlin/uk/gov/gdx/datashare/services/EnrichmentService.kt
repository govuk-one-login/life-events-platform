package uk.gov.gdx.datashare.services

import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType

interface EnrichmentService {
  fun accepts(eventType: EventType): Boolean
  fun process(
    eventType: EventType,
    dataId: String,
    enrichmentFields: List<EnrichmentField>,
  ): Any?
}
