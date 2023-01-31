package uk.gov.gdx.datashare.repositories

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.gdx.datashare.enums.EventType
import java.util.*

@Repository
interface PublisherSubscriptionRepository : CrudRepository<PublisherSubscription, UUID> {
  @Query("SELECT * FROM publisher_subscription ps where ps.client_id = :clientId and ps.event_type = :eventType")
  fun findByClientIdAndEventType(clientId: String, eventType: EventType): PublisherSubscription?

  @Query(
    "SELECT ps.* FROM publisher_subscription ps " +
      "JOIN publisher p ON ps.publisher_id = p.id " +
      "AND p.id = :id",
  )
  fun findAllByPublisherId(id: UUID): List<PublisherSubscription>

  @Query(
    "SELECT ps.* FROM publisher_subscription ps " +
      "JOIN publisher p ON ps.publisher_id = p.id and ps.event_type = :eventType",
  )
  fun findAllByPublisherNameAndEventType(eventType: EventType): List<PublisherSubscription>
}
