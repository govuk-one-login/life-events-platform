package uk.gov.gdx.datashare.uk.gov.gdx.datashare.helpers.builders

import uk.gov.gdx.datashare.repositories.SupplierEvent
import java.time.LocalDateTime
import java.util.*

data class SupplierEventBuilder(
  var id: UUID = UUID.randomUUID(),
  var supplierSubscriptionId: UUID = UUID.randomUUID(),
  var dataId: String = "",
  var eventTime: LocalDateTime? = null,
  var createdAt: LocalDateTime = LocalDateTime.now(),
  var deletedAt: LocalDateTime? = null,
  var new: Boolean = true,
) {
  fun build(): SupplierEvent {
    return SupplierEvent(
      id = id,
      supplierSubscriptionId = supplierSubscriptionId,
      dataId = dataId,
      eventTime = eventTime,
      createdAt = createdAt,
      deletedAt = deletedAt,
      new = new,
    )
  }
}
