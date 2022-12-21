package uk.gov.gdx.datashare.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface EgressEventDataRepository : CoroutineCrudRepository<EgressEventData, UUID> {

  @Query("SELECT ed.* FROM egress_event_data ed " +
    "WHERE ed.when_created > :fromTime " +
    "AND ed.when_created <= :toTime " +
    "AND ed.type_id = :eventTypeId " +
    "ORDER BY ed.when_created")
  fun findAllByEventType(eventTypeId: UUID, fromTime: LocalDateTime, toTime: LocalDateTime): Flow<EgressEventData>

  @Query("DELETE FROM egress_event_data where data_expiry_time < :expiredTime")
  @Modifying
  suspend fun deleteAllExpiredEvents(expiredTime: LocalDateTime)
}
