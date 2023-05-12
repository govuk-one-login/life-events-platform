package uk.gov.gdx.datashare.repositories

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface SupplierEventRepository : CrudRepository<SupplierEvent, UUID> {

  @Query(
    "SELECT ae.supplier_event_id  " +
      "FROM acquirer_event ae JOIN supplier_event se on ae.supplier_event_id = se.id and se.event_consumed = false " +
      "GROUP BY ae.supplier_event_id " +
      "HAVING SUM( CASE when ae.deleted_at is null then 1 else 0 END ) = 0",
  )
  fun findAllByDeletedEventsForAllAcquirers(): List<UUID>

  @Query("UPDATE supplier_event SET event_consumed = true WHERE id = :id")
  @Modifying
  fun markAsFullyConsumed(id: UUID)

  fun findAllByCreatedAtBeforeAndEventConsumedIsFalse(createdBy: LocalDateTime): List<SupplierEvent>
}
