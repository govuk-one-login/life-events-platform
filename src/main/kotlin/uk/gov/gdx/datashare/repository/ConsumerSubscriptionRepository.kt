package uk.gov.gdx.datashare.repository

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ConsumerSubscriptionRepository : CrudRepository<ConsumerSubscription, UUID> {
  @Query("SELECT * FROM consumer_subscription cs WHERE cs.oauth_client_id = :clientId")
  fun findAllByClientId(clientId: String): List<ConsumerSubscription>

  @Query(
    "SELECT * FROM consumer_subscription cs " +
      "WHERE cs.event_type = :eventType",
  )
  fun findAllByEventType(eventType: String): List<ConsumerSubscription>

  @Query(
    "SELECT * FROM consumer_subscription cs " +
      "WHERE cs.oauth_client_id = :clientId " +
      "AND cs.event_type IN (:eventTypes)",
  )
  fun findAllByEventTypesAndClientId(
    clientId: String,
    eventTypes: List<String>,
  ): List<ConsumerSubscription>

  @Query(
    "SELECT cs.* FROM consumer_subscription cs " +
      "JOIN event_data ed ON cs.id = ed.consumer_subscription_id " +
      "WHERE ed.id = :id ",
  )
  fun findByEventId(id: UUID): ConsumerSubscription?

  @Query(
    "SELECT cs.* FROM consumer_subscription cs " +
      "JOIN consumer c ON cs.consumer_id = c.id " +
      "AND c.id = :id",
  )
  fun findAllByConsumerId(id: UUID): List<ConsumerSubscription>
}
