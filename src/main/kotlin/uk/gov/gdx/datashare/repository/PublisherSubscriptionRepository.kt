package uk.gov.gdx.datashare.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PublisherSubscriptionRepository : CoroutineCrudRepository<PublisherSubscription, UUID> {

  @Query("SELECT * FROM publisher_subscription ps where ps.client_id = :clientId and ps.event_type_id = :eventType")
  suspend fun findByClientIdAndEventType(clientId: String, eventType: String): PublisherSubscription?

  @Query(
    "SELECT ps.* FROM publisher_subscription ps " +
      "JOIN publisher p ON ps.publisher_id = p.id " +
      "AND p.id = :id",
  )
  fun findAllByPublisherId(id: UUID): Flow<PublisherSubscription>
}
