package uk.gov.gdx.datashare.repositories

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface SupplierEventRepository : CrudRepository<SupplierEvent, UUID> {

  @Query("UPDATE supplier_event SET event_consumed = true WHERE id = :id")
  @Modifying
  fun markAsFullyConsumed(id: UUID)

  fun findAllByCreatedAtBeforeAndEventConsumedIsFalse(createdBy: LocalDateTime): List<SupplierEvent>
}
