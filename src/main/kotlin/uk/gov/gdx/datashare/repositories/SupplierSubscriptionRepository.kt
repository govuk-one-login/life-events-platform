package uk.gov.gdx.datashare.repositories

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.gdx.datashare.enums.EventType
import java.util.*

@Repository
interface SupplierSubscriptionRepository : CrudRepository<SupplierSubscription, UUID> {
  fun findByClientIdAndEventType(clientId: String, eventType: EventType): SupplierSubscription?

  fun findAllBySupplierId(id: UUID): List<SupplierSubscription>

  fun findFirstByEventType(eventType: EventType): SupplierSubscription?
}
