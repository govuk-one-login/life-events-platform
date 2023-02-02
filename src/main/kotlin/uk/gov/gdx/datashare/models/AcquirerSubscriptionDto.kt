package uk.gov.gdx.datashare.models

import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType
import java.time.LocalDateTime
import java.util.*

data class AcquirerSubscriptionDto(
  val acquirerSubscriptionId: UUID,
  val acquirerId: UUID,
  val oauthClientId: String? = null,
  val eventType: EventType,
  val enrichmentFields: List<EnrichmentField>,
  val enrichmentFieldsIncludedInPoll: Boolean,
  val whenCreated: LocalDateTime,
)
