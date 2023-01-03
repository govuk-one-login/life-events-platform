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
  @Query("SELECT * FROM consumer_subscription cs WHERE cs.poll_client_id = :clientId")
  fun findAllByPollClientId(clientId: String): Flow<ConsumerSubscription>

  @Query(
    "SELECT * FROM consumer_subscription cs " +
      "WHERE cs.ingress_event_type = :ingressEventType"
  )
  fun findAllByIngressEventType(ingressEventType: String): Flow<ConsumerSubscription>

  @Query(
    "SELECT * FROM consumer_subscription cs " +
      "WHERE cs.poll_client_id = :clientId " +
      "AND cs.ingress_event_type IN (:ingressEventTypes)"
  )
  fun findAllByIngressEventTypesAndPollClientId(
    clientId: String,
    ingressEventTypes: List<String>
  ): Flow<ConsumerSubscription>

  @Query(
    "SELECT * FROM consumer_subscription cs " +
      "WHERE cs.ingress_event_type = :eventType " +
      "AND cs.push_uri IS NOT NULL "
  )
  fun findClientToSendDataTo(eventType: String): Flow<ConsumerSubscription>

  @Query("UPDATE consumer_subscription SET last_poll_event_time = :lastTime WHERE id = :id")
  @Modifying
  suspend fun updateLastPollTime(lastPollEventTime: LocalDateTime, id: UUID)

  @Query(
    "SELECT cs.* FROM consumer_subscription cs " +
      "JOIN egress_event_data eed ON cs.id = eed.consumer_subscription_id " +
      "WHERE eed.id = :id "
  )
  suspend fun findByEgressEventId(id: UUID): ConsumerSubscription?

  @Query(
    "SELECT cs.* FROM consumer_subscription cs " +
      "JOIN consumer c ON cs.consumer_id = c.id " +
      "AND c.id = :id"
  )
  fun findAllByConsumerId(id: UUID): Flow<ConsumerSubscription>
}
