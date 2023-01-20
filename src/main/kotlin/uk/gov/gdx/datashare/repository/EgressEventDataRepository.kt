package uk.gov.gdx.datashare.repository

import kotlinx.coroutines.flow.Flow
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
      "ORDER BY ed.when_created",
  )
  fun findAllByConsumerSubscription(
    consumerSubscriptionId: UUID,
    fromTime: LocalDateTime,
    toTime: LocalDateTime,
  ): Flow<EgressEventData>

  @Query(
    "SELECT ed.* FROM egress_event_data ed " +
      "WHERE ed.when_created > :fromTime " +
      "AND ed.when_created <= :toTime " +
      "AND ed.consumer_subscription_id IN (:consumerSubscriptionIds) " +
      "ORDER BY ed.when_created " +
      "LIMIT :pageSize " +
      "OFFSET :offset",
  )
  fun findAllByConsumerSubscriptions(
    consumerSubscriptionIds: List<UUID>,
    fromTime: LocalDateTime,
    toTime: LocalDateTime,
    pageSize: Int,
    offset: Int,
  ): Flow<EgressEventData>

  @Query(
    "SELECT ed.* FROM egress_event_data ed " +
      "JOIN consumer_subscription cs on ed.consumer_subscription_id = cs.id " +
      "AND cs.oauth_client_id = :clientId " +
      "WHERE ed.when_created > :fromTime " +
      "AND ed.when_created <= :toTime " +
      "ORDER BY ed.when_created",
  )
  fun findAllByClientId(
    clientId: String,
    fromTime: LocalDateTime,
    toTime: LocalDateTime,
  ): Flow<EgressEventData>

  @Query(
    "SELECT ed.* FROM egress_event_data ed " +
      "JOIN consumer_subscription cs on ed.consumer_subscription_id = cs.id " +
      "AND cs.oauth_client_id = :clientId " +
      "WHERE ed.id = :id",
  )
  suspend fun findByClientIdAndId(clientId: String, id: UUID): EgressEventData?

  @Query(
    "SELECT ed.* FROM egress_event_data ed " +
      "WHERE ed.ingress_event_id = :ingressEventId",
  )
  fun findAllByIngressEventId(ingressEventId: UUID): Flow<EgressEventData>

  @Query(
    "SELECT ed.* FROM egress_event_data ed " +
      "JOIN consumer_subscription cs on ed.consumer_subscription_id = cs.id " +
      "JOIN consumer c ON cs.consumer_id = c.id " +
      "AND c.name = :name",
  )
  fun findAllByConsumerName(name: String): Flow<EgressEventData>
}
