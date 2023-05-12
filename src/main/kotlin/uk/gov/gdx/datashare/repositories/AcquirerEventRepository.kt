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

  @Override
  override fun findById(id: UUID): Optional<AcquirerEvent> = findByIdAndDeletedAtIsNull(id)
  fun findByIdAndDeletedAtIsNull(id: UUID): Optional<AcquirerEvent>

  @Override
  override fun findAll(): List<AcquirerEvent> = findAllByDeletedAtIsNull()
  fun findAllByDeletedAtIsNull(): List<AcquirerEvent>

  @Query("UPDATE acquirer_event SET deleted_at=:deletionTime WHERE id = :id")
  @Modifying
  fun softDeleteById(id: UUID, deletionTime: LocalDateTime)

  fun countByDeletedAtIsNull(): Int
}
