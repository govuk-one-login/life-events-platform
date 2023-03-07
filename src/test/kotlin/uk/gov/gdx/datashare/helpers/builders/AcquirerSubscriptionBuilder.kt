package uk.gov.gdx.datashare.uk.gov.gdx.datashare.helpers.builders

import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.repositories.AcquirerSubscription
import java.time.LocalDateTime
import java.util.*

data class AcquirerSubscriptionBuilder(
  var acquirerSubscriptionId: UUID = UUID.randomUUID(),
  var acquirerId: UUID = UUID.randomUUID(),
  var oauthClientId: String? = null,
  var eventType: EventType = EventType.DEATH_NOTIFICATION,
  var enrichmentFieldsIncludedInPoll: Boolean = false,
  var queueName: String? = null,
  var whenCreated: LocalDateTime = LocalDateTime.now(),
  var new: Boolean = true,
) {
  fun build(): AcquirerSubscription {
    return AcquirerSubscription(
      acquirerSubscriptionId = acquirerSubscriptionId,
      acquirerId = acquirerId,
      oauthClientId = oauthClientId,
      eventType = eventType,
      enrichmentFieldsIncludedInPoll = enrichmentFieldsIncludedInPoll,
      whenCreated = whenCreated,
      new = new,
    )
  }
}
