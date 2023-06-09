package uk.gov.gdx.datashare.uk.gov.gdx.datashare.integration.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.repositories.*
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.helpers.builders.*
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.helpers.compareIgnoringNanos
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.integration.MockIntegrationTestBase
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList

class AcquirerEventRepositoryTest(
  @Autowired private val acquirerRepository: AcquirerRepository,
  @Autowired private val acquirerSubscriptionRepository: AcquirerSubscriptionRepository,
  @Autowired private val acquirerEventRepository: AcquirerEventRepository,
  @Autowired private val supplierRepository: SupplierRepository,
  @Autowired private val supplierSubscriptionRepository: SupplierSubscriptionRepository,
  @Autowired private val supplierEventRepository: SupplierEventRepository,
) : MockIntegrationTestBase() {

  private val supplierSubscriptionId = UUID.randomUUID()

  private val acquirerSubscriptionId = UUID.randomUUID()
  private val eventType = EventType.TEST_EVENT
  private val oauthClientId = "client-id"

  private val otherAcquirerSubscriptionId = UUID.randomUUID()
  private val otherOauthClientId = "other-client-id"

  private val thirdAcquirerSubscriptionId = UUID.randomUUID()

  private val fromTime = LocalDateTime.now().minusDays(6)
  private val timeInRange = LocalDateTime.now().minusDays(4)
  private val toTime = LocalDateTime.now().minusDays(2)

  @Test
  fun `findPageByAcquirerSubscriptions finds paginated offset events`() {
    setupSupplierAndAcquirerSubscriptions()

    val acquirerEvents = mutableListOf<AcquirerEvent>()
    val otherAcquirerEvents = mutableListOf<AcquirerEvent>()
    for (i in 1..4) {
      val timeOffset = (10 * i).toLong()
      acquirerEvents.add(saveAcquirerEventForSubscription(timeInRange.plusMinutes(timeOffset)))
      otherAcquirerEvents.add(saveAcquirerEventForOtherSubscription(timeInRange.plusMinutes(timeOffset + 1)))
      saveAcquirerEventForThirdSubscription(timeInRange.plusMinutes(timeOffset))
      saveDeletedAcquirerEventForSubscription(timeInRange.plusMinutes(timeOffset))
      saveTooEarlyAcquirerEventForSubscription()
      saveTooLateAcquirerEventForSubscription()
    }

    val events = acquirerEventRepository.findPageByAcquirerSubscriptions(
      listOf(acquirerSubscriptionId, otherAcquirerSubscriptionId),
      fromTime,
      toTime,
      2,
      4,
    )

    assertThat(events).hasSize(2)
    val firstEvent = events[0]
    val secondEvent = events[1]
    assertThat(
      firstEvent,
    )
      .usingRecursiveComparison()
      .ignoringFields("new")
      .withComparatorForType(compareIgnoringNanos, LocalDateTime::class.java)
      .isEqualTo(acquirerEvents[2])
    assertThat(
      secondEvent,
    )
      .usingRecursiveComparison()
      .ignoringFields("new")
      .withComparatorForType(compareIgnoringNanos, LocalDateTime::class.java)
      .isEqualTo(otherAcquirerEvents[2])
  }

  @Test
  fun `countByAcquirerSubscriptions counts not deleted events for acquirer subscription in a time window`() {
    setupSupplierAndAcquirerSubscriptions()

    saveAcquirerEventForSubscription()
    saveAcquirerEventForOtherSubscription()
    saveAcquirerEventForThirdSubscription()
    saveTooEarlyAcquirerEventForSubscription()
    saveTooLateAcquirerEventForSubscription()
    saveDeletedAcquirerEventForSubscription()

    val count = acquirerEventRepository.countByAcquirerSubscriptions(
      listOf(acquirerSubscriptionId, otherAcquirerSubscriptionId),
      fromTime,
      toTime,
    )

    assertThat(count).isEqualTo(2)
  }

  @Test
  fun `findByClientIdAndId returns the correct value`() {
    setupSupplierAndAcquirerSubscriptions()

    val acquirerEvent = saveAcquirerEventForSubscription()
    val deletedAcquirerEvent = saveDeletedAcquirerEventForSubscription()

    assertThat(
      acquirerEventRepository.findByClientIdAndId(oauthClientId, acquirerEvent.id),
    )
      .usingRecursiveComparison()
      .ignoringFields("new")
      .withComparatorForType(compareIgnoringNanos, LocalDateTime::class.java)
      .isEqualTo(acquirerEvent)

    assertThat(
      acquirerEventRepository.findByClientIdAndId(oauthClientId, deletedAcquirerEvent.id),
    ).isNull()

    assertThat(
      acquirerEventRepository.findByClientIdAndId(otherOauthClientId, acquirerEvent.id),
    ).isNull()
  }

  @Test
  fun `softDeleteById soft deletes event`() {
    setupSupplierAndAcquirerSubscriptions()

    val acquirerEvent = saveAcquirerEventForSubscription()

    val deletionTime = LocalDateTime.now()

    acquirerEventRepository.softDeleteById(acquirerEvent.id, deletionTime)

    assertThat(
      acquirerEventRepository.findByIdOrNull(acquirerEvent.id)?.deletedAt?.truncatedTo(ChronoUnit.MILLIS),
    )
      .isEqualTo(deletionTime.truncatedTo(ChronoUnit.MILLIS))
  }

  @Test
  fun `softDeleteById doesn't update deleted event`() {
    setupSupplierAndAcquirerSubscriptions()

    val oldDeletionTime = LocalDateTime.now().minusDays(4)

    val acquirerEvent = saveDeletedAcquirerEventForSubscription(deletedAt = oldDeletionTime)

    val deletionTime = LocalDateTime.now()

    acquirerEventRepository.softDeleteById(acquirerEvent.id, deletionTime)

    assertThat(
      acquirerEventRepository.findByIdOrNull(acquirerEvent.id)?.deletedAt?.truncatedTo(ChronoUnit.MILLIS),
    )
      .isEqualTo(oldDeletionTime.truncatedTo(ChronoUnit.MILLIS))
  }

  @Test
  fun `softDeleteByAllByAcquirerSubscriptionId soft deletes all events`() {
    setupSupplierAndAcquirerSubscriptions()

    val events = ArrayList<AcquirerEvent>()
    for (i in 1..3) {
      events.add(saveAcquirerEventForSubscription())
    }

    val deletionTime = LocalDateTime.now()

    acquirerEventRepository.softDeleteAllByAcquirerSubscriptionId(acquirerSubscriptionId, deletionTime)

    events.forEach { event ->
      assertThat(
        acquirerEventRepository.findByIdOrNull(event.id)?.deletedAt?.truncatedTo(ChronoUnit.MILLIS),
      )
        .isEqualTo(deletionTime.truncatedTo(ChronoUnit.MILLIS))
    }
  }

  @Test
  fun `softDeleteByAllByAcquirerSubscriptionId doesn't update deleted event`() {
    setupSupplierAndAcquirerSubscriptions()

    val oldDeletionTime = LocalDateTime.now().minusDays(4)

    val events = ArrayList<AcquirerEvent>()
    for (i in 1..3) {
      events.add(saveDeletedAcquirerEventForSubscription(deletedAt = oldDeletionTime))
    }

    val deletionTime = LocalDateTime.now()

    acquirerEventRepository.softDeleteAllByAcquirerSubscriptionId(acquirerSubscriptionId, deletionTime)

    events.forEach { event ->
      assertThat(
        acquirerEventRepository.findByIdOrNull(event.id)?.deletedAt?.truncatedTo(ChronoUnit.MILLIS),
      )
        .isEqualTo(oldDeletionTime.truncatedTo(ChronoUnit.MILLIS))
    }
  }

  @Test
  fun `countByDeletedAtIsNullForSubscriptions counts not deleted events`() {
    setupSupplierAndAcquirerSubscriptions()

    saveAcquirerEventForSubscription()
    saveAcquirerEventForSubscription()
    saveAcquirerEventForOtherSubscription()
    saveDeletedAcquirerEventForSubscription()

    val countsForSubscriptions = acquirerEventRepository.countByDeletedAtIsNullForSubscriptions()

    assertThat(countsForSubscriptions.any { it.acquirerSubscriptionId == acquirerSubscriptionId && it.count == 2 }).isTrue()
    assertThat(countsForSubscriptions.any { it.acquirerSubscriptionId == otherAcquirerSubscriptionId && it.count == 1 }).isTrue()
  }

  private fun setupSupplierAndAcquirerSubscriptions() {
    val acquirer = acquirerRepository.save(AcquirerBuilder().build())
    acquirerSubscriptionRepository.saveAll(
      listOf(
        AcquirerSubscriptionBuilder(
          acquirerSubscriptionId = acquirerSubscriptionId,
          eventType = eventType,
          oauthClientId = oauthClientId,
          acquirerId = acquirer.id,
        ).build(),
        AcquirerSubscriptionBuilder(
          acquirerSubscriptionId = otherAcquirerSubscriptionId,
          eventType = EventType.DEATH_NOTIFICATION,
          oauthClientId = otherOauthClientId,
          acquirerId = acquirer.id,
        ).build(),
        AcquirerSubscriptionBuilder(
          acquirerSubscriptionId = thirdAcquirerSubscriptionId,
          eventType = EventType.GRO_DEATH_NOTIFICATION,
          oauthClientId = "third-client-id",
          acquirerId = acquirer.id,
        ).build(),
      ),
    )
    val supplier = supplierRepository.save(SupplierBuilder().build())
    val supplierSubscription = supplierSubscriptionRepository.save(
      SupplierSubscriptionBuilder(
        supplierSubscriptionId = supplierSubscriptionId,
        supplierId = supplier.id,
      ).build(),
    )
    supplierEventRepository.save(
      SupplierEventBuilder(
        supplierSubscriptionId = supplierSubscription.id,
      ).build(),
    )
  }

  private fun saveAcquirerEventForSubscription(createdAt: LocalDateTime = timeInRange) = acquirerEventRepository.save(
    AcquirerEventBuilder(
      acquirerSubscriptionId = acquirerSubscriptionId,
      supplierEventId = saveSupplierEvent().id,
      createdAt = createdAt,
    ).build(),
  )

  private fun saveAcquirerEventForOtherSubscription(createdAt: LocalDateTime = timeInRange) =
    acquirerEventRepository.save(
      AcquirerEventBuilder(
        acquirerSubscriptionId = otherAcquirerSubscriptionId,
        supplierEventId = saveSupplierEvent().id,
        createdAt = createdAt,
      ).build(),
    )

  private fun saveAcquirerEventForThirdSubscription(createdAt: LocalDateTime = timeInRange) =
    acquirerEventRepository.save(
      AcquirerEventBuilder(
        acquirerSubscriptionId = thirdAcquirerSubscriptionId,
        supplierEventId = saveSupplierEvent().id,
        createdAt = createdAt,
      ).build(),
    )

  private fun saveTooEarlyAcquirerEventForSubscription() = acquirerEventRepository.save(
    AcquirerEventBuilder(
      acquirerSubscriptionId = acquirerSubscriptionId,
      supplierEventId = saveSupplierEvent().id,
      createdAt = fromTime.minusDays(1),
    ).build(),
  )

  private fun saveTooLateAcquirerEventForSubscription() = acquirerEventRepository.save(
    AcquirerEventBuilder(
      acquirerSubscriptionId = acquirerSubscriptionId,
      supplierEventId = saveSupplierEvent().id,
      createdAt = toTime.plusDays(1),
    ).build(),
  )

  private fun saveDeletedAcquirerEventForSubscription(
    createdAt: LocalDateTime = timeInRange,
    deletedAt: LocalDateTime = LocalDateTime.now(),
  ) = acquirerEventRepository.save(
    AcquirerEventBuilder(
      acquirerSubscriptionId = acquirerSubscriptionId,
      supplierEventId = saveSupplierEvent().id,
      createdAt = createdAt,
      deletedAt = deletedAt,
    ).build(),
  )

  private fun saveSupplierEvent() = supplierEventRepository.save(
    SupplierEventBuilder(
      supplierSubscriptionId = supplierSubscriptionId,
    ).build(),
  )
}
