package uk.gov.gdx.datashare.repository

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PublisherSubscriptionRepository : CoroutineCrudRepository<EventSubscription, Long> {

  @Query("SELECT * FROM publisher_subscription ps where ps.client_id = :clientId and ps.event_type_id = :eventType")
  suspend fun findByClientIdAndEventType(clientId: String, eventType: String): EventSubscription?
}
