package uk.gov.gdx.datashare.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface ConsumerSubscriptionRepository : CoroutineCrudRepository<ConsumerSubscription, UUID> {

  @Query("SELECT * FROM consumer_subscription cs where cs.callback_client_id = :clientId and cs.event_type_id = :eventType")
  suspend fun findByClientIdAndEventType(clientId: String, eventType: UUID): ConsumerSubscription?

  @Query("SELECT * FROM consumer_subscription cs where cs.poll_client_id = :clientId")
  fun findAllByPollerClientId(clientId: String): Flow<ConsumerSubscription>

  @Query("SELECT * FROM consumer_subscription cs " +
    "JOIN egress_event_type eet ON cs.event_type_id = eet.id " +
    "AND eet.ingress_event_type = :eventType " +
    "WHERE cs.push_uri IS NOT NULL ")
  fun findClientToSendDataTo(eventType: String): Flow<ConsumerSubscription>

  @Query("UPDATE consumer_subscription set last_poll_event_time = :lastTime where consumer_id = :consumerId and event_type_id = :eventType")
  @Modifying
  suspend fun updateLastPollTime(lastPollEventTime: LocalDateTime, consumerId: UUID, eventType: UUID)

  @Query("SELECT cs.* FROM consumer_subscription cs " +
    "JOIN egress_event_type eet ON cs.event_type_id = eet.id " +
    "JOIN egress_event_data eed ON eet.id = eed.type_id " +
    "WHERE eed.id = :id ")
  suspend fun findByEgressEventId(id: UUID): ConsumerSubscription?
}
