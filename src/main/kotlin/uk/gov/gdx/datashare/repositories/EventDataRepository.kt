package uk.gov.gdx.datashare.repositories

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface EventDataRepository : CrudRepository<EventData, UUID> {
  @Query(
    "SELECT ed.* FROM event_data ed " +
      "WHERE ed.when_created > :fromTime " +
      "AND ed.when_created <= :toTime " +
      "AND ed.consumer_subscription_id = :consumerSubscriptionId " +
      "AND ed.deleted_at is null " +
      "ORDER BY ed.when_created",
  )
  fun findAllByConsumerSubscription(
    consumerSubscriptionId: UUID,
    fromTime: LocalDateTime,
    toTime: LocalDateTime,
  ): List<EventData>

  @Query(
    "SELECT ed.* FROM event_data ed " +
      "WHERE ed.when_created > :fromTime " +
      "AND ed.when_created <= :toTime " +
      "AND ed.consumer_subscription_id IN (:consumerSubscriptionIds) " +
      "AND ed.deleted_at is null " +
      "ORDER BY ed.when_created " +
      "LIMIT :pageSize " +
      "OFFSET :offset",
  )
  fun findPageByConsumerSubscriptions(
    consumerSubscriptionIds: List<UUID>,
    fromTime: LocalDateTime,
    toTime: LocalDateTime,
    pageSize: Int,
    offset: Int,
  ): List<EventData>

  @Query(
    "SELECT COUNT(ed.*) FROM event_data ed " +
      "WHERE ed.when_created > :fromTime " +
      "AND ed.when_created <= :toTime " +
      "AND ed.consumer_subscription_id IN (:consumerSubscriptionIds) " +
      "AND ed.deleted_at is null",
  )
  fun countByConsumerSubscriptions(
    consumerSubscriptionIds: List<UUID>,
    fromTime: LocalDateTime,
    toTime: LocalDateTime,
  ): Int

  @Query(
    "SELECT ed.* FROM event_data ed " +
      "JOIN consumer_subscription cs on ed.consumer_subscription_id = cs.id " +
      "AND cs.oauth_client_id = :clientId " +
      "WHERE ed.id = :id " +
      "AND ed.deleted_at is null",
  )
  fun findByClientIdAndId(clientId: String, id: UUID): EventData?

  @Override
  override fun findById(id: UUID): Optional<EventData> = findByEventIdAndDeletedAtIsNull(id)
  fun findByEventIdAndDeletedAtIsNull(id: UUID): Optional<EventData>

  @Override
  override fun findAll(): List<EventData> = findAllByDeletedAtIsNull()
  fun findAllByDeletedAtIsNull(): List<EventData>

  @Query("UPDATE event_data SET deleted_at=:deletionTime WHERE id = :id")
  @Modifying
  fun softDeleteById(id: UUID, deletionTime: LocalDateTime)
}
