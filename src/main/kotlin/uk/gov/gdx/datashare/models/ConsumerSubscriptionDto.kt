package uk.gov.gdx.datashare.models

import uk.gov.gdx.datashare.enums.EventType
import java.time.LocalDateTime
import java.util.*

data class ConsumerSubscriptionDto(
  val consumerSubscriptionId: UUID,
  val consumerId: UUID,
  val oauthClientId: String? = null,
  val eventType: EventType,
  val enrichmentFields: List<String>,
  val enrichmentFieldsIncludedInPoll: Boolean,
  val whenCreated: LocalDateTime,
)
