package uk.gov.gdx.datashare.repositories

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SupplierEventRepository : CrudRepository<SupplierEvent, UUID> {
  @Query(
    "SELECT se FROM supplier_event se " +
      "JOIN supplier_subscription ss ON ss.id = se.supplier_subscription_id " +
      "LEFT JOIN acquirer_event ae ON ae.supplier_event_id = se.id " +
      "WHERE se.deleted_at IS NULL " +
      "AND ss.event_type = 'GRO_DEATH_NOTIFICATION'" +
      "GROUP BY se.id " +
      "HAVING SUM( CASE WHEN ae.deleted_at IS NULL THEN 1 ELSE 0 END ) = 0",
  )
  fun findGroDeathEventsForDeletion(): List<SupplierEvent>
}
