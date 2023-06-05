package uk.gov.gdx.datashare.uk.gov.gdx.datashare.helpers.builders

import uk.gov.gdx.datashare.repositories.AcquirerEvent
import java.time.LocalDateTime
import java.util.*

data class AcquirerEventBuilder(
  var id: UUID = UUID.randomUUID(),
  var supplierEventId: UUID = UUID.randomUUID(),
  var acquirerSubscriptionId: UUID = UUID.randomUUID(),
  var dataId: String = "",
  var eventTime: LocalDateTime? = null,
  var createdAt: LocalDateTime = LocalDateTime.now(),
  var deletedAt: LocalDateTime? = null,
  var new: Boolean = true,
) {
  fun build(): AcquirerEvent {
    return AcquirerEvent(
      id = id,
      supplierEventId = supplierEventId,
      acquirerSubscriptionId = acquirerSubscriptionId,
      dataId = dataId,
      eventTime = eventTime,
      createdAt = createdAt,
      deletedAt = deletedAt,
      new = new,
    )
  }
}
