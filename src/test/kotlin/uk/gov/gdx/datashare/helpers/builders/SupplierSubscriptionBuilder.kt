package uk.gov.gdx.datashare.uk.gov.gdx.datashare.helpers.builders

import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.repositories.SupplierSubscription
import java.time.LocalDateTime
import java.util.*

data class SupplierSubscriptionBuilder(
  var supplierSubscriptionId: UUID = UUID.randomUUID(),
  var supplierId: UUID = UUID.randomUUID(),
  var clientId: String = "test-client-id",
  var eventType: EventType = EventType.DEATH_NOTIFICATION,
  var whenCreated: LocalDateTime = LocalDateTime.now(),
  var whenDeleted: LocalDateTime? = null,
  var new: Boolean = true,
) {
  fun build(): SupplierSubscription {
    return SupplierSubscription(
      supplierSubscriptionId = supplierSubscriptionId,
      supplierId = supplierId,
      clientId = clientId,
      eventType = eventType,
      whenCreated = whenCreated,
      whenDeleted = whenDeleted,
      new = new,
    )
  }
}
