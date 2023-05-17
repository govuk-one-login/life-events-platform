package uk.gov.gdx.datashare.repositories

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SupplierEventRepository : CrudRepository<SupplierEvent, UUID> {
  @Query(
    "SELECT se.* FROM supplier_event se " +
      "JOIN supplier_subscription ss ON ss.id = se.supplier_subscription_id " +
      "WHERE se.deleted_at IS NULL " +
      "AND ss.event_type = 'GRO_DEATH_NOTIFICATION' " +
      "AND NOT EXISTS (" +
      "SELECT 1 FROM acquirer_event ae WHERE ae.deleted_at IS NULL AND ae.supplier_event_id = se.id " +
      ") " +
      "ORDER BY random()",
  )
  fun findGroDeathEventsForDeletion(): List<SupplierEvent>
}
