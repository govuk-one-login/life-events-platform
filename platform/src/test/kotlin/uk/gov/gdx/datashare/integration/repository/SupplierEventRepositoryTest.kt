package uk.gov.gdx.datashare.integration.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.helpers.builders.*
import uk.gov.gdx.datashare.integration.MockIntegrationTestBase
import uk.gov.gdx.datashare.repositories.*
import java.time.LocalDateTime

class SupplierEventRepositoryTest(
  @Autowired private val acquirerRepository: AcquirerRepository,
  @Autowired private val acquirerSubscriptionRepository: AcquirerSubscriptionRepository,
  @Autowired private val acquirerEventRepository: AcquirerEventRepository,
  @Autowired private val supplierRepository: SupplierRepository,
  @Autowired private val supplierSubscriptionRepository: SupplierSubscriptionRepository,
  @Autowired private val supplierEventRepository: SupplierEventRepository,
) : MockIntegrationTestBase() {
  @Test
  fun `findGroDeathEventsForDeletion returns the correct values`() {
    val supplier = supplierRepository.save(SupplierBuilder().build())
    val groSubscription = supplierSubscriptionRepository.save(
      SupplierSubscriptionBuilder(
        supplierId = supplier.id,
        eventType = EventType.GRO_DEATH_NOTIFICATION,
      ).build(),
    )
    val nonGroSubscription = supplierSubscriptionRepository.save(
      SupplierSubscriptionBuilder(
        supplierId = supplier.id,
        eventType = EventType.DEATH_NOTIFICATION,
      ).build(),
    )

    val returnedSupplierEvent =
      supplierEventRepository.save(
        SupplierEventBuilder(
          supplierSubscriptionId = groSubscription.id,
          dataId = "test1",
        ).build(),
      )
    val nonGroSupplierEvent =
      supplierEventRepository.save(
        SupplierEventBuilder(
          supplierSubscriptionId = nonGroSubscription.id,
          dataId = "test2",
        ).build(),
      )
    val deletedSupplierEvent =
      supplierEventRepository.save(
        SupplierEventBuilder(
          supplierSubscriptionId = groSubscription.id,
          deletedAt = LocalDateTime.now(),
          dataId = "test3",
        ).build(),
      )
    val supplierEventWithAcquirerEvent =
      supplierEventRepository.save(
        SupplierEventBuilder(
          supplierSubscriptionId = groSubscription.id,
          dataId = "test4",
        ).build(),
      )

    val acquirer = acquirerRepository.save(AcquirerBuilder().build())
    val acquirerSubscription =
      acquirerSubscriptionRepository.save(AcquirerSubscriptionBuilder(acquirerId = acquirer.id).build())
    acquirerEventRepository.save(
      AcquirerEventBuilder(
        acquirerSubscriptionId = acquirerSubscription.id,
        supplierEventId = supplierEventWithAcquirerEvent.id,
      ).build(),
    )

    val events = supplierEventRepository.findGroDeathEventsForDeletion()

    assertThat(
      events.filter { e -> e.id == returnedSupplierEvent.id },
    )
      .hasSize(1)

    assertThat(
      events.filter { e -> e.id == nonGroSupplierEvent.id },
    )
      .isEmpty()

    assertThat(
      events.filter { e -> e.id == deletedSupplierEvent.id },
    )
      .isEmpty()

    assertThat(
      events.filter { e -> e.id == supplierEventWithAcquirerEvent.id },
    )
      .isEmpty()
  }
}
