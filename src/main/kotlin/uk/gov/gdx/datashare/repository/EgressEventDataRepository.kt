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

  @Query("SELECT ed.* FROM egress_event_data ed " +
    "WHERE ed.when_created > :fromTime " +
    "AND ed.when_created <= :toTime " +
    "AND ed.type_id IN (:eventTypeIds) " +
    "ORDER BY ed.when_created")
  fun findAllByEventTypes(eventTypeIds: List<UUID>, fromTime: LocalDateTime, toTime: LocalDateTime): Flow<EgressEventData>

  @Query("DELETE FROM egress_event_data where data_expiry_time < :expiredTime")
  @Modifying
  suspend fun deleteAllExpiredEvents(expiredTime: LocalDateTime)

  @Query("SELECT ed.* FROM egress_event_data ed " +
    "JOIN egress_event_type eet on ed.type_id = eet.id " +
    "JOIN consumer_subscription cs on eet.id = cs.event_type_id " +
    "AND cs.poll_client_id = :pollerClientId " +
    "WHERE ed.when_created > :fromTime " +
    "AND ed.when_created <= :toTime " +
    "ORDER BY ed.when_created")
  fun findAllByPollerClientId(pollerClientId: String, fromTime: LocalDateTime, toTime: LocalDateTime): Flow<EgressEventData>

  @Query("SELECT ed.* FROM egress_event_data ed " +
    "JOIN egress_event_type eet on ed.type_id = eet.id " +
    "JOIN consumer_subscription cs on eet.id = cs.event_type_id " +
    "AND cs.poll_client_id = :pollerClientId " +
    "WHERE ed.id = :id")
  suspend fun findByPollerClientIdAndId(pollerClientId: String, id: UUID): EgressEventData?

  @Query("SELECT ed.* FROM egress_event_data ed " +
    "WHERE ed.ingress_event_id = :ingressEventId")
  fun findAllByIngressEventId(ingressEventId: UUID): Flow<EgressEventData>

  @Query("SELECT cs.is_legacy FROM egress_event_data ed " +
    "JOIN egress_event_type eet on ed.type_id = eet.id " +
    "JOIN consumer_subscription cs on eet.id = cs.event_type_id")
  suspend fun isLegacyEvent(id: UUID): Boolean
}
