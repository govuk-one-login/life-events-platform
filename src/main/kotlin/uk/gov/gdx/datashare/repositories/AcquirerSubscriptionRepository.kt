package uk.gov.gdx.datashare.repositories

import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.gdx.datashare.enums.EventType
import java.util.*

@Repository
@JaversSpringDataAuditable
interface AcquirerSubscriptionRepository : CrudRepository<AcquirerSubscription, UUID> {
  fun findAllByOauthClientId(oauthClientId: String): List<AcquirerSubscription>

  fun findAllByEventType(eventType: EventType): List<AcquirerSubscription>

  fun findByAcquirerSubscriptionIdAndQueueNameIsNotNull(acquirerSubscriptionId: UUID): AcquirerSubscription?

  fun findAllByOauthClientIdAndEventTypeIsIn(
    oauthClientId: String,
    eventTypes: List<EventType>,
  ): List<AcquirerSubscription>

  @Query(
    "SELECT asub.* FROM acquirer_subscription asub " +
      "JOIN acquirer_event ae ON asub.id = ae.acquirer_subscription_id " +
      "WHERE ae.id = :id ",
  )
  fun findByEventId(id: UUID): AcquirerSubscription?

  @Query(
    "SELECT asub.* FROM acquirer_subscription asub " +
      "JOIN acquirer a ON asub.acquirer_id = a.id " +
      "AND a.id = :id",
  )
  fun findAllByAcquirerId(id: UUID): List<AcquirerSubscription>
}
