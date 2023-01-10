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

  @Query(
    "SELECT ed.* FROM egress_event_data ed " +
      "WHERE ed.when_created > :fromTime " +
      "AND ed.when_created <= :toTime " +
      "AND ed.consumer_subscription_id = :consumerSubscriptionId " +
      "ORDER BY ed.when_created"
  )
  fun findAllByConsumerSubscription(
    consumerSubscriptionId: UUID,
    fromTime: LocalDateTime,
    toTime: LocalDateTime
  ): Flow<EgressEventData>

  @Query(
    "SELECT ed.* FROM egress_event_data ed " +
      "WHERE ed.when_created > :fromTime " +
      "AND ed.when_created <= :toTime " +
      "AND ed.consumer_subscription_id IN (:consumerSubscriptionIds) " +
      "ORDER BY ed.when_created"
  )
  fun findAllByConsumerSubscriptions(
    consumerSubscriptionIds: List<UUID>,
    fromTime: LocalDateTime,
    toTime: LocalDateTime
  ): Flow<EgressEventData>

  @Query("DELETE FROM egress_event_data where data_expiry_time < :expiredTime")
  @Modifying
  suspend fun deleteAllExpiredEvents(expiredTime: LocalDateTime)

  @Query(
    "SELECT ed.* FROM egress_event_data ed " +
      "JOIN consumer_subscription cs on ed.consumer_subscription_id = cs.id " +
      "AND cs.poll_client_id = :pollerClientId " +
      "WHERE ed.when_created > :fromTime " +
      "AND ed.when_created <= :toTime " +
      "ORDER BY ed.when_created"
  )
  fun findAllByPollClientId(
    pollerClientId: String,
    fromTime: LocalDateTime,
    toTime: LocalDateTime
  ): Flow<EgressEventData>

  @Query(
    "SELECT ed.* FROM egress_event_data ed " +
      "JOIN consumer_subscription cs on ed.consumer_subscription_id = cs.id " +
      "AND cs.poll_client_id = :pollerClientId " +
      "WHERE ed.id = :id"
  )
  suspend fun findByPollClientIdAndId(pollerClientId: String, id: UUID): EgressEventData?

  @Query(
    "SELECT ed.* FROM egress_event_data ed " +
      "JOIN consumer_subscription cs on ed.consumer_subscription_id = cs.id " +
      "AND cs.callback_client_id = :callbackClientId " +
      "WHERE ed.id = :id"
  )
  suspend fun findByCallbackClientIdAndId(callbackClientId: String, id: UUID): EgressEventData?


  @Query(
    "SELECT ed.* FROM egress_event_data ed " +
      "WHERE ed.ingress_event_id = :ingressEventId"
  )
  fun findAllByIngressEventId(ingressEventId: UUID): Flow<EgressEventData>
}
