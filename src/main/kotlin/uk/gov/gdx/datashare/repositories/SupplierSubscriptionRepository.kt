package uk.gov.gdx.datashare.repositories

import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.gdx.datashare.enums.EventType
import java.util.*

@Repository
@JaversSpringDataAuditable
interface SupplierSubscriptionRepository : CrudRepository<SupplierSubscription, UUID> {
  fun findByClientIdAndEventType(clientId: String, eventType: EventType): SupplierSubscription?

  fun findAllBySupplierId(id: UUID): List<SupplierSubscription>

  fun findFirstByEventType(eventType: EventType): SupplierSubscription?
}
