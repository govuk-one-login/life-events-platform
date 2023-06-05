package uk.gov.gdx.datashare.uk.gov.gdx.datashare.integration.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.repositories.*
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.helpers.builders.*
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.helpers.compareIgnoringNanos
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.integration.MockIntegrationTestBase
import java.time.LocalDateTime

class AcquirerSubscriptionRepositoryTest(
  @Autowired private val acquirerRepository: AcquirerRepository,
  @Autowired private val acquirerSubscriptionRepository: AcquirerSubscriptionRepository,
  @Autowired private val acquirerEventRepository: AcquirerEventRepository,
  @Autowired private val supplierRepository: SupplierRepository,
  @Autowired private val supplierSubscriptionRepository: SupplierSubscriptionRepository,
  @Autowired private val supplierEventRepository: SupplierEventRepository,
) : MockIntegrationTestBase() {

  @Test
  fun `findByAcquirerSubscriptionIdAndQueueNameIsNotNull returns the correct values`() {
    val acquirer = acquirerRepository.save(AcquirerBuilder().build())
    val returnedSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder(acquirerId = acquirer.id, queueName = "test").build(),
    )
    val notReturnedSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder(acquirerId = acquirer.id, queueName = null).build(),
    )

    assertThat(
      acquirerSubscriptionRepository.findByAcquirerSubscriptionIdAndQueueNameIsNotNull(returnedSubscription.acquirerSubscriptionId),
    )
      .usingRecursiveComparison()
      .ignoringFields("new")
      .withComparatorForType(compareIgnoringNanos, LocalDateTime::class.java)
      .isEqualTo(returnedSubscription)

    assertThat(
      acquirerSubscriptionRepository.findByAcquirerSubscriptionIdAndQueueNameIsNotNull(notReturnedSubscription.acquirerSubscriptionId),
    ).isNull()
  }

  @Test
  fun `findAllByEventTypeAndWhenDeletedIsNull returns the correct values`() {
    val acquirer = acquirerRepository.save(AcquirerBuilder().build())
    val returnedSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder(
        acquirerId = acquirer.id,
        queueName = "test",
        eventType = EventType.DEATH_NOTIFICATION,
      ).build(),
    )
    val notReturnedSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder(
        acquirerId = acquirer.id,
        queueName = null,
        eventType = EventType.DEATH_NOTIFICATION,
        whenDeleted = LocalDateTime.now(),
      ).build(),
    )

    val subscriptions =
      acquirerSubscriptionRepository.findAllByEventTypeAndWhenDeletedIsNull(EventType.DEATH_NOTIFICATION)

    assertThat(
      subscriptions.filter { s -> s.acquirerSubscriptionId == returnedSubscription.id },
    )
      .hasSize(1)

    assertThat(
      subscriptions.filter { s -> s.acquirerSubscriptionId == notReturnedSubscription.id },
    )
      .isEmpty()
  }

  @Test
  fun `findAllByWhenDeletedIsNull returns the correct values`() {
    val acquirer = acquirerRepository.save(AcquirerBuilder().build())
    val returnedSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder(acquirerId = acquirer.id).build(),
    )
    val notReturnedSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder(
        acquirerId = acquirer.id,
        whenDeleted = LocalDateTime.now(),
      ).build(),
    )

    val subscriptions =
      acquirerSubscriptionRepository.findAllByWhenDeletedIsNull()

    assertThat(
      subscriptions.filter { s -> s.acquirerSubscriptionId == returnedSubscription.id },
    )
      .hasSize(1)

    assertThat(
      subscriptions.filter { s -> s.acquirerSubscriptionId == notReturnedSubscription.id },
    )
      .isEmpty()
  }

  @Test
  fun `findAllByOauthClientIdAndWhenDeletedIsNull returns the correct values`() {
    val acquirer = acquirerRepository.save(AcquirerBuilder().build())
    val returnedSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder(
        acquirerId = acquirer.id,
        oauthClientId = "test",
      ).build(),
    )
    val whenDeletedNotNullSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder(
        acquirerId = acquirer.id,
        oauthClientId = "test",
        whenDeleted = LocalDateTime.now(),
      ).build(),
    )
    val incorrectClientIdSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder(
        acquirerId = acquirer.id,
        oauthClientId = null,
      ).build(),
    )

    val subscriptions =
      acquirerSubscriptionRepository.findAllByOauthClientIdAndWhenDeletedIsNull("test")

    assertThat(
      subscriptions.filter { s -> s.acquirerSubscriptionId == returnedSubscription.id },
    )
      .hasSize(1)

    assertThat(
      subscriptions.filter { s -> s.acquirerSubscriptionId == whenDeletedNotNullSubscription.id || s.acquirerSubscriptionId == incorrectClientIdSubscription.id },
    )
      .isEmpty()
  }

  @Test
  fun `findAllByQueueNameAndWhenDeletedIsNull returns the correct values`() {
    val acquirer = acquirerRepository.save(AcquirerBuilder().build())
    val returnedSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder(
        acquirerId = acquirer.id,
        queueName = "test",
      ).build(),
    )
    val whenDeletedNotNullSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder(
        acquirerId = acquirer.id,
        queueName = "test",
        whenDeleted = LocalDateTime.now(),
      ).build(),
    )
    val incorrectQueueNameSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder(
        acquirerId = acquirer.id,
        queueName = null,
      ).build(),
    )

    val subscriptions =
      acquirerSubscriptionRepository.findAllByQueueNameAndWhenDeletedIsNull("test")

    assertThat(
      subscriptions.filter { s -> s.acquirerSubscriptionId == returnedSubscription.id },
    )
      .hasSize(1)

    assertThat(
      subscriptions.filter { s -> s.acquirerSubscriptionId == whenDeletedNotNullSubscription.id || s.acquirerSubscriptionId == incorrectQueueNameSubscription.id },
    )
      .isEmpty()
  }

  @Test
  fun `findAllByOauthClientIdAndWhenDeletedIsNullAndEventTypeIsIn returns the correct values`() {
    val acquirer = acquirerRepository.save(AcquirerBuilder().build())
    val returnedSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder(
        acquirerId = acquirer.id,
        oauthClientId = "test",
        eventType = EventType.DEATH_NOTIFICATION,
      ).build(),
    )
    val whenDeletedNotNullSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder(
        acquirerId = acquirer.id,
        oauthClientId = "test",
        eventType = EventType.DEATH_NOTIFICATION,
        whenDeleted = LocalDateTime.now(),
      ).build(),
    )
    val incorrectClientIdSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder(
        acquirerId = acquirer.id,
        oauthClientId = null,
        eventType = EventType.DEATH_NOTIFICATION,
      ).build(),
    )
    val incorrectEventTypeSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder(
        acquirerId = acquirer.id,
        oauthClientId = "test",
        eventType = EventType.LIFE_EVENT,
      ).build(),
    )

    val subscriptions =
      acquirerSubscriptionRepository.findAllByOauthClientIdAndWhenDeletedIsNullAndEventTypeIsIn(
        "test",
        listOf(EventType.DEATH_NOTIFICATION),
      )

    assertThat(
      subscriptions.filter { s -> s.acquirerSubscriptionId == returnedSubscription.id },
    )
      .hasSize(1)

    assertThat(
      subscriptions.filter { s -> s.acquirerSubscriptionId == whenDeletedNotNullSubscription.id || s.acquirerSubscriptionId == incorrectClientIdSubscription.id || s.acquirerSubscriptionId == incorrectEventTypeSubscription.id },
    )
      .isEmpty()
  }

  @Test
  fun `findByEventId returns the correct value`() {
    val acquirer = acquirerRepository.save(AcquirerBuilder().build())
    val returnedSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder(acquirerId = acquirer.id).build(),
    )
    val notReturnedSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder(acquirerId = acquirer.id).build(),
    )

    val supplier = supplierRepository.save(SupplierBuilder().build())
    val supplierSubscription =
      supplierSubscriptionRepository.save(SupplierSubscriptionBuilder(supplierId = supplier.id).build())
    val supplierEvent =
      supplierEventRepository.save(
        SupplierEventBuilder(
          supplierSubscriptionId = supplierSubscription.id,
          new = true,
        ).build(),
      )

    val acquirerEvent = acquirerEventRepository.save(
      AcquirerEventBuilder(
        acquirerSubscriptionId = returnedSubscription.id,
        supplierEventId = supplierEvent.id,
        new = true,
      ).build(),
    )

    assertThat(
      acquirerSubscriptionRepository.findByEventId(acquirerEvent.id),
    )
      .usingRecursiveComparison()
      .ignoringFields("new")
      .withComparatorForType(compareIgnoringNanos, LocalDateTime::class.java)
      .isEqualTo(returnedSubscription)
      .isNotEqualTo(notReturnedSubscription)
  }

  @Test
  fun `findByEventId does not return a deleted subscription`() {
    val acquirer = acquirerRepository.save(AcquirerBuilder().build())
    val returnedSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder(
        acquirerId = acquirer.id,
        whenDeleted = LocalDateTime.now(),
      ).build(),
    )

    val supplier = supplierRepository.save(SupplierBuilder().build())
    val supplierSubscription =
      supplierSubscriptionRepository.save(SupplierSubscriptionBuilder(supplierId = supplier.id).build())
    val supplierEvent =
      supplierEventRepository.save(
        SupplierEventBuilder(
          supplierSubscriptionId = supplierSubscription.id,
          new = true,
        ).build(),
      )

    val acquirerEvent = acquirerEventRepository.save(
      AcquirerEventBuilder(
        acquirerSubscriptionId = returnedSubscription.id,
        supplierEventId = supplierEvent.id,
        new = true,
      ).build(),
    )

    assertThat(
      acquirerSubscriptionRepository.findByEventId(acquirerEvent.id),
    )
      .isEqualTo(null)
  }

  @Test
  fun `findAllByAcquirerId returns the correct values`() {
    val checkedAcquirer = acquirerRepository.save(AcquirerBuilder().build())
    val notCheckedAcquirer = acquirerRepository.save(AcquirerBuilder().build())
    val returnedSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder(acquirerId = checkedAcquirer.id).build(),
    )
    val subscriptionWithIncorrectAcquirer = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder(acquirerId = notCheckedAcquirer.id).build(),
    )
    val deletedSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder(
        acquirerId = checkedAcquirer.id,
        whenDeleted = LocalDateTime.now(),
      ).build(),
    )

    val subscriptions = acquirerSubscriptionRepository.findAllByAcquirerId(checkedAcquirer.id)

    assertThat(
      subscriptions.filter { s -> s.acquirerSubscriptionId == returnedSubscription.id },
    )
      .hasSize(1)

    assertThat(
      subscriptions.filter { s -> s.acquirerSubscriptionId == subscriptionWithIncorrectAcquirer.id || s.acquirerSubscriptionId == deletedSubscription.id },
    )
      .isEmpty()
  }
}
