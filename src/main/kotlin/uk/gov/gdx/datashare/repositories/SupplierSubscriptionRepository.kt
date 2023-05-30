package uk.gov.gdx.datashare.repositories

import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.gdx.datashare.enums.EventType
import java.util.*

@Repository
@JaversSpringDataAuditable
interface SupplierSubscriptionRepository : CrudRepository<SupplierSubscription, UUID> {
  @Override
  override fun findById(id: UUID) = findBySupplierSubscriptionIdAndWhenDeletedIsNull(id)
  fun findBySupplierSubscriptionIdAndWhenDeletedIsNull(id: UUID): Optional<SupplierSubscription>

  @Override
  override fun findAll() = findAllByWhenDeletedIsNull()
  fun findAllByWhenDeletedIsNull(): List<SupplierSubscription>

  fun findAllByClientId(clientId: String) = findAllByClientIdAndWhenDeletedIsNull(clientId)
  fun findAllByClientIdAndWhenDeletedIsNull(clientId: String): List<SupplierSubscription>

  fun findByClientIdAndEventType(clientId: String, eventType: EventType) = findByClientIdAndEventTypeAndWhenDeletedIsNull(clientId, eventType)
  fun findByClientIdAndEventTypeAndWhenDeletedIsNull(clientId: String, eventType: EventType): SupplierSubscription?

  fun findAllBySupplierId(id: UUID) = findAllBySupplierIdAndWhenDeletedIsNull(id)
  fun findAllBySupplierIdAndWhenDeletedIsNull(id: UUID): List<SupplierSubscription>

  fun findFirstByEventType(eventType: EventType) = findFirstByEventTypeAndWhenDeletedIsNull(eventType)
  fun findFirstByEventTypeAndWhenDeletedIsNull(eventType: EventType): SupplierSubscription?
}
