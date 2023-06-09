package uk.gov.gdx.datashare.repositories

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface AcquirerEventRepository : CrudRepository<AcquirerEvent, UUID> {
  @Query(
    "SELECT ae.* FROM acquirer_event ae " +
      "WHERE ae.created_at > :fromTime " +
      "AND ae.created_at <= :toTime " +
      "AND ae.acquirer_subscription_id IN (:acquirerSubscriptionIds) " +
      "AND ae.deleted_at is null " +
      "ORDER BY ae.created_at " +
      "LIMIT :pageSize " +
      "OFFSET :offset",
  )
  fun findPageByAcquirerSubscriptions(
    acquirerSubscriptionIds: List<UUID>,
    fromTime: LocalDateTime,
    toTime: LocalDateTime,
    pageSize: Int,
    offset: Int,
  ): List<AcquirerEvent>

  @Query(
    "SELECT COUNT(ae.*) FROM acquirer_event ae " +
      "WHERE ae.created_at > :fromTime " +
      "AND ae.created_at <= :toTime " +
      "AND ae.acquirer_subscription_id IN (:acquirerSubscriptionIds) " +
      "AND ae.deleted_at is null",
  )
  fun countByAcquirerSubscriptions(
    acquirerSubscriptionIds: List<UUID>,
    fromTime: LocalDateTime,
    toTime: LocalDateTime,
  ): Int

  @Query(
    "SELECT ae.* FROM acquirer_event ae " +
      "JOIN acquirer_subscription asub on ae.acquirer_subscription_id = asub.id " +
      "AND asub.oauth_client_id = :clientId " +
      "WHERE ae.id = :id " +
      "AND ae.deleted_at is null",
  )
  fun findByClientIdAndId(clientId: String, id: UUID): AcquirerEvent?

  @Query("UPDATE acquirer_event SET deleted_at=:deletionTime WHERE id = :id")
  @Modifying
  fun softDeleteById(id: UUID, deletionTime: LocalDateTime)

  @Query(
    "UPDATE acquirer_event " +
      "SET deleted_at=:deletionTime " +
      "WHERE acquirer_subscription_id = :acquirerSubscriptionId",
  )
  @Modifying
  fun softDeleteAllByAcquirerSubscriptionId(acquirerSubscriptionId: UUID, deletionTime: LocalDateTime)

  @Query(
    "SELECT acquirer_subscription_id, COUNT(CASE WHEN deleted_at IS NULL THEN 1 END) " +
      "FROM acquirer_event ae " +
      "JOIN acquirer_subscription asub ON ae.acquirer_subscription_id = asub.id " +
      "WHERE asub.when_deleted IS NULL " +
      "GROUP BY acquirer_subscription_id",
  )
  fun countByDeletedAtIsNullForSubscriptions(): List<SubscriptionsCount>
}

data class SubscriptionsCount(
  val acquirerSubscriptionId: UUID,
  val count: Int,
)
