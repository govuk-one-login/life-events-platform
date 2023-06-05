package uk.gov.gdx.datashare.uk.gov.gdx.datashare.integration.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import uk.gov.gdx.datashare.repositories.*
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.helpers.builders.*
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.helpers.compareIgnoringNanos
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.integration.MockIntegrationTestBase
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class AcquirerEventRepositoryTest(
  @Autowired private val acquirerRepository: AcquirerRepository,
  @Autowired private val acquirerSubscriptionRepository: AcquirerSubscriptionRepository,
  @Autowired private val acquirerEventRepository: AcquirerEventRepository,
  @Autowired private val supplierRepository: SupplierRepository,
  @Autowired private val supplierSubscriptionRepository: SupplierSubscriptionRepository,
  @Autowired private val supplierEventRepository: SupplierEventRepository,
) : MockIntegrationTestBase() {
  @Test
  fun `findByClientIdAndId returns the correct value`() {
    val acquirer = acquirerRepository.save(AcquirerBuilder().build())
    val returnedSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder().also {
        it.acquirerId = acquirer.id
        it.oauthClientId = "test"
      }.build(),
    )
    val notReturnedSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder().also {
        it.acquirerId = acquirer.id
        it.oauthClientId = "not-returned-test"
      }.build(),
    )


    val supplier = supplierRepository.save(SupplierBuilder().build())
    val supplierSubscription =
      supplierSubscriptionRepository.save(SupplierSubscriptionBuilder(supplierId = supplier.id).build())
    val supplierEvent =
      supplierEventRepository.save(
        SupplierEventBuilder(
          supplierSubscriptionId = supplierSubscription.id,
        ).build(),
      )

    val deletedEvent = acquirerEventRepository.save(
      AcquirerEventBuilder(
        acquirerSubscriptionId = notReturnedSubscription.id,
        deletedAt = LocalDateTime.now(),
        supplierEventId = supplierEvent.id,
      ).build(),
    )
    val returnedEvent = acquirerEventRepository.save(
      AcquirerEventBuilder(
        acquirerSubscriptionId = returnedSubscription.id,
        supplierEventId = supplierEvent.id,
      ).build(),
    )

    assertThat(
      acquirerEventRepository.findByClientIdAndId("test", returnedEvent.id),
    )
      .usingRecursiveComparison()
      .ignoringFields("new")
      .withComparatorForType(compareIgnoringNanos, LocalDateTime::class.java)
      .isEqualTo(returnedEvent)

    assertThat(
      acquirerEventRepository.findByClientIdAndId("not-returned-test", returnedEvent.id),
    ).isNull()

    assertThat(
      acquirerEventRepository.findByClientIdAndId("not-returned-test", deletedEvent.id),
    ).isNull()
  }

  @Test
  fun `softDeleteById soft deletes event`() {
    val acquirer = acquirerRepository.save(AcquirerBuilder().build())
    val subscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder().also {
        it.acquirerId = acquirer.id
        it.oauthClientId = "test"
      }.build(),
    )


    val supplier = supplierRepository.save(SupplierBuilder().build())
    val supplierSubscription =
      supplierSubscriptionRepository.save(SupplierSubscriptionBuilder(supplierId = supplier.id).build())
    val supplierEvent =
      supplierEventRepository.save(
        SupplierEventBuilder(
          supplierSubscriptionId = supplierSubscription.id,
        ).build(),
      )

    val event = acquirerEventRepository.save(
      AcquirerEventBuilder(
        acquirerSubscriptionId = subscription.id,
        supplierEventId = supplierEvent.id,
        deletedAt = null,
      ).build(),
    )

    val deletionTime = LocalDateTime.now()

    acquirerEventRepository.softDeleteById(event.id, deletionTime)

    assertThat(
      acquirerEventRepository.findByIdOrNull(event.id)?.deletedAt?.truncatedTo(ChronoUnit.MILLIS),
    )
      .isEqualTo(deletionTime.truncatedTo(ChronoUnit.MILLIS))
  }

  @Test
  fun `softDeleteByAllByAcquirerSubscriptionId soft deletes all events`() {
    val acquirer = acquirerRepository.save(AcquirerBuilder().build())
    val subscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder().also {
        it.acquirerId = acquirer.id
        it.oauthClientId = "test"
      }.build(),
    )


    val supplier = supplierRepository.save(SupplierBuilder().build())
    val supplierSubscription =
      supplierSubscriptionRepository.save(SupplierSubscriptionBuilder(supplierId = supplier.id).build())
    val supplierEvent =
      supplierEventRepository.save(
        SupplierEventBuilder(
          supplierSubscriptionId = supplierSubscription.id,
        ).build(),
      )

    val events = ArrayList<AcquirerEvent>()
    for (i in 1..3) {
      events.add(
        acquirerEventRepository.save(
          AcquirerEventBuilder(
            acquirerSubscriptionId = subscription.id,
            supplierEventId = supplierEvent.id,
            deletedAt = null,
          ).build(),
        ),
      )
    }

    val deletionTime = LocalDateTime.now()

    acquirerEventRepository.softDeleteAllByAcquirerSubscriptionId(subscription.id, deletionTime)

    events.forEach { event ->
      assertThat(
        acquirerEventRepository.findByIdOrNull(event.id)?.deletedAt?.truncatedTo(ChronoUnit.MILLIS),
      )
        .isEqualTo(deletionTime.truncatedTo(ChronoUnit.MILLIS))
    }
  }

}
