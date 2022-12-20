package uk.gov.gdx.datashare.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface EventDataRepository : CoroutineCrudRepository<EventData, String> {

  @Query("SELECT ed.* FROM event_data ed where ed.event_type_id = :eventType and ed.when_created > :fromTime and ed.when_created <= :toTime order by when_created")
  fun findAllByEventType(eventType: String, fromTime: LocalDateTime, toTime: LocalDateTime): Flow<EventData>

  @Query("DELETE FROM event_data where data_expiry_time < :expiredTime")
  @Modifying
  suspend fun deleteAllExpiredEvents(expiredTime: LocalDateTime)
}
