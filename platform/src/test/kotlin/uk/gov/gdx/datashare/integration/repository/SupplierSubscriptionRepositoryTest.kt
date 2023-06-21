package uk.gov.gdx.datashare.integration.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.repositories.*
import uk.gov.gdx.datashare.helpers.builders.*
import uk.gov.gdx.datashare.helpers.compareIgnoringNanos
import uk.gov.gdx.datashare.integration.MockIntegrationTestBase
import java.time.LocalDateTime

class SupplierSubscriptionRepositoryTest(
  @Autowired private val supplierRepository: SupplierRepository,
  @Autowired private val supplierSubscriptionRepository: SupplierSubscriptionRepository,
) : MockIntegrationTestBase() {

  @Test
  fun `findAllByWhenDeletedIsNull returns the correct values`() {
    val supplier = supplierRepository.save(SupplierBuilder().build())
    val returnedSubscription = supplierSubscriptionRepository.save(
      SupplierSubscriptionBuilder(supplierId = supplier.id, clientId = "client-one").build(),
    )
    val notReturnedSubscription = supplierSubscriptionRepository.save(
      SupplierSubscriptionBuilder(
        supplierId = supplier.id,
        clientId = "client-two",
        whenDeleted = LocalDateTime.now(),
      ).build(),
    )

    val subscriptions =
      supplierSubscriptionRepository.findAllByWhenDeletedIsNull()

    assertThat(
      subscriptions.filter { s -> s.supplierSubscriptionId == returnedSubscription.id },
    )
      .hasSize(1)

    assertThat(
      subscriptions.filter { s -> s.supplierSubscriptionId == notReturnedSubscription.id },
    )
      .isEmpty()
  }

  @Test
  fun `findAllByClientIdAndWhenDeletedIsNull returns the correct values`() {
    val supplier = supplierRepository.save(SupplierBuilder().build())
    val returnedSubscription = supplierSubscriptionRepository.save(
      SupplierSubscriptionBuilder(
        supplierId = supplier.id,
        clientId = "test",
        eventType = EventType.DEATH_NOTIFICATION,
      ).build(),
    )
    // Client ID/Event Type pair must be unique
    val whenDeletedNotNullSubscription = supplierSubscriptionRepository.save(
      SupplierSubscriptionBuilder(
        supplierId = supplier.id,
        clientId = "test",
        eventType = EventType.TEST_EVENT,
        whenDeleted = LocalDateTime.now(),
      ).build(),
    )
    val incorrectClientIdSubscription = supplierSubscriptionRepository.save(
      SupplierSubscriptionBuilder(
        supplierId = supplier.id,
        clientId = "test-two",
      ).build(),
    )

    val subscriptions =
      supplierSubscriptionRepository.findAllByClientIdAndWhenDeletedIsNull("test")

    assertThat(
      subscriptions.filter { s -> s.supplierSubscriptionId == returnedSubscription.id },
    )
      .hasSize(1)

    assertThat(
      subscriptions.filter { s -> s.supplierSubscriptionId == whenDeletedNotNullSubscription.id },
    )
      .isEmpty()

    assertThat(
      subscriptions.filter { s -> s.supplierSubscriptionId == incorrectClientIdSubscription.id },
    )
      .isEmpty()
  }

  @Test
  fun `findByClientIdAndEventTypeAndWhenDeletedIsNull returns the correct value`() {
    val supplier = supplierRepository.save(SupplierBuilder().build())
    val returnedSubscription = supplierSubscriptionRepository.save(
      SupplierSubscriptionBuilder(
        supplierId = supplier.id,
        clientId = "test",
        eventType = EventType.DEATH_NOTIFICATION,
      ).build(),
    )
    val incorrectClientIdSubscription = supplierSubscriptionRepository.save(
      SupplierSubscriptionBuilder(
        supplierId = supplier.id,
        clientId = "test-two",
        eventType = EventType.DEATH_NOTIFICATION,
      ).build(),
    )
    val incorrectEventTypeSubscription = supplierSubscriptionRepository.save(
      SupplierSubscriptionBuilder(
        supplierId = supplier.id,
        clientId = "test",
        eventType = EventType.LIFE_EVENT,
      ).build(),
    )

    assertThat(
      supplierSubscriptionRepository.findByClientIdAndEventTypeAndWhenDeletedIsNull(
        "test",
        EventType.DEATH_NOTIFICATION,
      ),
    )
      .usingRecursiveComparison()
      .ignoringFields("new")
      .withComparatorForType(compareIgnoringNanos, LocalDateTime::class.java)
      .isEqualTo(returnedSubscription)
      .isNotEqualTo(incorrectClientIdSubscription)
      .isNotEqualTo(incorrectEventTypeSubscription)
  }

  @Test
  fun `findByClientIdAndEventTypeAndWhenDeletedIsNull does not return a deleted subscription`() {
    val supplier = supplierRepository.save(SupplierBuilder().build())
    supplierSubscriptionRepository.save(
      SupplierSubscriptionBuilder(
        supplierId = supplier.id,
        clientId = "test",
        eventType = EventType.DEATH_NOTIFICATION,
        whenDeleted = LocalDateTime.now(),
      ).build(),
    )

    val subscription = supplierSubscriptionRepository.findByClientIdAndEventTypeAndWhenDeletedIsNull(
      "test",
      EventType.DEATH_NOTIFICATION,
    )

    assertThat(subscription).isNull()
  }

  @Test
  fun `findAllBySupplierIdAndWhenDeletedIsNull returns the correct values`() {
    val checkedSupplier = supplierRepository.save(SupplierBuilder().build())
    val notCheckedSupplier = supplierRepository.save(SupplierBuilder().build())
    val returnedSubscription = supplierSubscriptionRepository.save(
      SupplierSubscriptionBuilder(
        supplierId = checkedSupplier.id,
        clientId = "client-one",
      ).build(),
    )
    val subscriptionWithIncorrectSupplier = supplierSubscriptionRepository.save(
      SupplierSubscriptionBuilder(
        supplierId = notCheckedSupplier.id,
        clientId = "client-two",
      ).build(),
    )
    val deletedSubscription = supplierSubscriptionRepository.save(
      SupplierSubscriptionBuilder(
        supplierId = checkedSupplier.id,
        whenDeleted = LocalDateTime.now(),
        clientId = "client-three",
      ).build(),
    )

    val subscriptions = supplierSubscriptionRepository.findAllBySupplierIdAndWhenDeletedIsNull(checkedSupplier.id)

    assertThat(
      subscriptions.filter { s -> s.supplierSubscriptionId == returnedSubscription.id },
    )
      .hasSize(1)

    assertThat(
      subscriptions.filter { s -> s.supplierSubscriptionId == subscriptionWithIncorrectSupplier.id },
    )
      .isEmpty()

    assertThat(
      subscriptions.filter { s -> s.supplierSubscriptionId == deletedSubscription.id },
    )
      .isEmpty()
  }
}
