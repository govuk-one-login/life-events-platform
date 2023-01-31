package uk.gov.gdx.datashare.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import uk.gov.gdx.datashare.config.SupplierSubscriptionNotFoundException
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.models.SupplierRequest
import uk.gov.gdx.datashare.models.SupplierSubRequest
import uk.gov.gdx.datashare.repositories.Supplier
import uk.gov.gdx.datashare.repositories.SupplierRepository
import uk.gov.gdx.datashare.repositories.SupplierSubscription
import uk.gov.gdx.datashare.repositories.SupplierSubscriptionRepository
import java.util.*

class SuppliersServiceTest {
  private val supplierSubscriptionRepository = mockk<SupplierSubscriptionRepository>()
  private val supplierRepository = mockk<SupplierRepository>()

  private val underTest = SuppliersService(supplierSubscriptionRepository, supplierRepository)

  @Test
  fun `getSuppliers gets all suppliers`() {
    val savedSuppliers = listOf(
      Supplier(name = "Supplier1"),
      Supplier(name = "Supplier2"),
      Supplier(name = "Supplier3"),
    )

    every { supplierRepository.findAll() }.returns(savedSuppliers)

    val suppliers = underTest.getSuppliers()

    assertThat(suppliers).hasSize(3)
    assertThat(suppliers).isEqualTo(savedSuppliers)
  }

  @Test
  fun `getSupplierSubscriptions gets all supplier subscriptions`() {
    val savedSupplierSubscriptions = listOf(
      SupplierSubscription(
        supplierId = UUID.randomUUID(),
        clientId = "Client-1",
        eventType = EventType.DEATH_NOTIFICATION,
      ),
      SupplierSubscription(
        supplierId = UUID.randomUUID(),
        clientId = "Client-2",
        eventType = EventType.DEATH_NOTIFICATION,
      ),
      SupplierSubscription(
        supplierId = UUID.randomUUID(),
        clientId = "Client-3",
        eventType = EventType.DEATH_NOTIFICATION,
      ),
    )

    every { supplierSubscriptionRepository.findAll() }.returns(savedSupplierSubscriptions)

    val supplierSubscriptions = underTest.getSupplierSubscriptions()

    assertThat(supplierSubscriptions).hasSize(3)
    assertThat(supplierSubscriptions).isEqualTo(savedSupplierSubscriptions)
  }

  @Test
  fun `getSubscriptionsForSupplier gets all supplier subscriptions for id`() {
    val savedSupplierSubscriptions = listOf(
      SupplierSubscription(
        supplierId = supplier.id,
        clientId = "Client-1",
        eventType = EventType.DEATH_NOTIFICATION,
      ),
      SupplierSubscription(
        supplierId = supplier.id,
        clientId = "Client-2",
        eventType = EventType.DEATH_NOTIFICATION,
      ),
      SupplierSubscription(
        supplierId = supplier.id,
        clientId = "Client-3",
        eventType = EventType.DEATH_NOTIFICATION,
      ),
    )

    every { supplierSubscriptionRepository.findAllBySupplierId(supplier.id) }.returns(savedSupplierSubscriptions)

    val supplierSubscriptions = underTest.getSubscriptionsForSupplier(supplier.id)

    assertThat(supplierSubscriptions).hasSize(3)
    assertThat(supplierSubscriptions).isEqualTo(savedSupplierSubscriptions)
  }

  @Test
  fun `addSupplierSubscription adds new subscription if supplier exists`() {
    every { supplierSubscriptionRepository.save(any()) }.returns(supplierSubscription)

    underTest.addSupplierSubscription(supplier.id, supplierSubRequest)

    verify(exactly = 1) {
      supplierSubscriptionRepository.save(
        withArg {
          assertThat(it.supplierId).isEqualTo(supplier.id)
          assertThat(it.clientId).isEqualTo(supplierSubRequest.clientId)
          assertThat(it.eventType).isEqualTo(supplierSubRequest.eventType)
        },
      )
    }
  }

  @Test
  fun `updateSupplierSubscription updates subscription`() {
    every { supplierSubscriptionRepository.findByIdOrNull(supplierSubscription.id) }.returns(supplierSubscription)
    every { supplierSubscriptionRepository.save(any()) }.returns(supplierSubscription)

    underTest.updateSupplierSubscription(supplier.id, supplierSubscription.id, supplierSubRequest)

    verify(exactly = 1) {
      supplierSubscriptionRepository.save(
        withArg {
          assertThat(it.supplierId).isEqualTo(supplier.id)
          assertThat(it.clientId).isEqualTo(supplierSubRequest.clientId)
          assertThat(it.eventType).isEqualTo(supplierSubRequest.eventType)
        },
      )
    }
  }

  @Test
  fun `updateSupplierSubscription does not update subscription if subscription does not exist`() {
    every { supplierSubscriptionRepository.findByIdOrNull(supplierSubscription.id) }.returns(null)
    val exception = assertThrows<SupplierSubscriptionNotFoundException> {
      underTest.updateSupplierSubscription(supplier.id, supplierSubscription.id, supplierSubRequest)
    }

    assertThat(exception.message).isEqualTo("Subscription ${supplierSubscription.id} not found")

    verify(exactly = 0) { supplierSubscriptionRepository.save(any()) }
  }

  @Test
  fun `addSupplier adds supplier`() {
    val supplierRequest = SupplierRequest(
      name = "Supplier",
    )

    every { supplierRepository.save(any()) }.returns(supplier)

    underTest.addSupplier(supplierRequest)

    verify(exactly = 1) {
      supplierRepository.save(
        withArg {
          assertThat(it.name).isEqualTo(supplierRequest.name)
        },
      )
    }
  }

  private val supplier = Supplier(name = "Base Supplier")
  private val supplierSubscription = SupplierSubscription(
    supplierId = supplier.id,
    clientId = "Client",
    eventType = EventType.DEATH_NOTIFICATION,
  )
  private val supplierSubRequest = SupplierSubRequest(
    clientId = "Client-New",
    eventType = EventType.LIFE_EVENT,
  )
}
