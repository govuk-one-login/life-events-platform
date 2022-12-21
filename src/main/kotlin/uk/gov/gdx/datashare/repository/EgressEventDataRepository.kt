package uk.gov.gdx.datashare.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface EgressEventDataRepository : CoroutineCrudRepository<EgressEventData, String> {

  @Query("SELECT ed.* FROM egress_event_data ed " +
    "JOIN egress_event_type et ON ed.type_id = et.id " +
    "AND et.ingress_event_type = :eventType " +
    "WHERE ed.when_created > :fromTime " +
    "AND ed.when_created <= :toTime " +
    "ORDER BY ed.when_created")
  fun findAllByEventType(eventType: String, fromTime: LocalDateTime, toTime: LocalDateTime): Flow<EventData>

  @Query("DELETE FROM egress_event_data where data_expiry_time < :expiredTime")
  @Modifying
  suspend fun deleteAllExpiredEvents(expiredTime: LocalDateTime)
}
