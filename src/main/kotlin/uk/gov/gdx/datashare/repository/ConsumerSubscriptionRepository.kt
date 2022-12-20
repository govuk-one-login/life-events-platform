package uk.gov.gdx.datashare.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ConsumerSubscriptionRepository : CoroutineCrudRepository<ConsumerSubscription, Long> {

  @Query("SELECT * FROM consumer_subscription cs where cs.callback_client_id = :clientId and cs.event_type_id = :eventType")
  suspend fun findByClientIdAndEventType(clientId: String, eventType: String): ConsumerSubscription?

  @Query("SELECT * FROM consumer_subscription cs where cs.poll_client_id = :clientId")
  fun findAllByPollerClientId(clientId: String): Flow<ConsumerSubscription>

  @Query("SELECT * FROM consumer_subscription cs where cs.push_uri is not null and cs.event_type_id = :eventType")
  fun findClientToSendDataTo(eventType: String): Flow<ConsumerSubscription>

  @Query("UPDATE consumer_subscription set last_poll_event_time = :lastTime where consumer_id = :consumerId and event_type_id = :eventType")
  @Modifying
  suspend fun updateLastPollTime(lastPollEventTime: LocalDateTime, consumerId: Long, eventType: String)
}
