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
  fun findAllByWhenDeletedIsNull(): List<AcquirerSubscription>

  fun findAllByQueueNameIsNotNullAndWhenDeletedIsNull(): List<AcquirerSubscription>

  fun findAllByOauthClientIdAndWhenDeletedIsNull(oauthClientId: String): List<AcquirerSubscription>

  fun findAllByEventTypeAndWhenDeletedIsNull(eventType: EventType): List<AcquirerSubscription>

  fun findAllByQueueNameAndWhenDeletedIsNull(queueName: String): List<AcquirerSubscription>

  fun findByAcquirerSubscriptionIdAndQueueNameIsNotNull(acquirerSubscriptionId: UUID): AcquirerSubscription?

  fun findAllByOauthClientIdAndWhenDeletedIsNullAndEventTypeIsIn(
    oauthClientId: String,
    eventTypes: List<EventType>,
  ): List<AcquirerSubscription>

  @Query(
    "SELECT asub.* FROM acquirer_subscription asub " +
      "JOIN acquirer_event ae ON asub.id = ae.acquirer_subscription_id " +
      "WHERE ae.id = :id AND asub.when_deleted IS NULL",
  )
  fun findByEventId(id: UUID): AcquirerSubscription?

  @Query(
    "SELECT asub.* FROM acquirer_subscription asub " +
      "JOIN acquirer a ON asub.acquirer_id = a.id " +
      "WHERE a.id = :id AND asub.when_deleted IS NULL",
  )
  fun findAllByAcquirerId(id: UUID): List<AcquirerSubscription>
}
