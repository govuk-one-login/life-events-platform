package uk.gov.gdx.datashare.repository

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PublisherSubscriptionRepository : CrudRepository<PublisherSubscription, UUID> {
  @Query("SELECT * FROM publisher_subscription ps where ps.client_id = :clientId and ps.event_type_id = :eventType")
  fun findByClientIdAndEventType(clientId: String, eventType: String): PublisherSubscription?

  @Query(
    "SELECT ps.* FROM publisher_subscription ps " +
      "JOIN publisher p ON ps.publisher_id = p.id " +
      "AND p.id = :id",
  )
  fun findAllByPublisherId(id: UUID): List<PublisherSubscription>
}
