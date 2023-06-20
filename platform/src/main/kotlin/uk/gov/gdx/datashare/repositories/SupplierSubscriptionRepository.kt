package uk.gov.gdx.datashare.repositories

import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.gdx.datashare.enums.EventType
import java.util.*

@Repository
@JaversSpringDataAuditable
interface SupplierSubscriptionRepository : CrudRepository<SupplierSubscription, UUID> {
  fun findAllByWhenDeletedIsNull(): List<SupplierSubscription>

  fun findAllByClientIdAndWhenDeletedIsNull(clientId: String): List<SupplierSubscription>

  fun findByClientIdAndEventTypeAndWhenDeletedIsNull(clientId: String, eventType: EventType): SupplierSubscription?

  fun findAllBySupplierIdAndWhenDeletedIsNull(id: UUID): List<SupplierSubscription>

  fun findFirstByEventTypeAndWhenDeletedIsNull(eventType: EventType): SupplierSubscription?
}
