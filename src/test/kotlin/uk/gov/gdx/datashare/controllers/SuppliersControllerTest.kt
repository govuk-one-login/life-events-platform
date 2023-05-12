package uk.gov.gdx.datashare.controllers

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.models.SupplierRequest
import uk.gov.gdx.datashare.models.SupplierSubRequest
import uk.gov.gdx.datashare.repositories.Supplier
import uk.gov.gdx.datashare.repositories.SupplierSubscription
import uk.gov.gdx.datashare.services.SuppliersService
import java.util.*

class SuppliersControllerTest {
  private val suppliersService = mockk<SuppliersService>()

  private val underTest = SuppliersController(suppliersService)

  @Test
  fun `getSuppliers gets suppliers`() {
    val suppliers = listOf(
      Supplier(
        name = "Supplier 1",
      ),
      Supplier(
        name = "Supplier 2",
      ),
    )

    every { suppliersService.getSuppliers() }.returns(suppliers)

    val suppliersOutput = underTest.getSuppliers().toList()

    assertThat(suppliersOutput).hasSize(2)
    assertThat(suppliersOutput).isEqualTo(suppliers.toList())
  }

  @Test
  fun `addSupplier adds supplier`() {
    val supplierRequest = SupplierRequest(
      name = "Supplier",
    )
    val supplier = Supplier(name = supplierRequest.name)

    every { suppliersService.addSupplier(any()) }.returns(supplier)

    val supplierOutput = underTest.addSupplier(supplierRequest)

    assertThat(supplierOutput).isEqualTo(supplier)
  }

  @Test
  fun `deleteSupplier deletes supplier`() {
    val supplier = Supplier(name = "Supplier Name")

    every { suppliersService.deleteSupplier(supplier.id) }.returns(supplier)

    val supplierOutput = underTest.deleteSupplier(supplier.id)

    assertThat(supplierOutput).isEqualTo(supplier)

    verify(exactly = 1) {
      suppliersService.deleteSupplier(supplier.id)
    }
  }

  @Test
  fun `getSupplierSubscriptions gets supplier subscriptions`() {
    val supplierSubscriptions = listOf(
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

    every { suppliersService.getSupplierSubscriptions() }.returns(supplierSubscriptions)

    val supplierSubscriptionsOutput = underTest.getSupplierSubscriptions().toList()

    assertThat(supplierSubscriptionsOutput).hasSize(3)
    assertThat(supplierSubscriptionsOutput).isEqualTo(supplierSubscriptions.toList())
  }

  @Test
  fun `getSubscriptionsForSupplier gets supplier subscriptions`() {
    val supplierId = UUID.randomUUID()
    val supplierSubscriptions = listOf(
      SupplierSubscription(
        supplierId = supplierId,
        clientId = "Client-1",
        eventType = EventType.DEATH_NOTIFICATION,
      ),
      SupplierSubscription(
        supplierId = supplierId,
        clientId = "Client-2",
        eventType = EventType.DEATH_NOTIFICATION,
      ),
      SupplierSubscription(
        supplierId = supplierId,
        clientId = "Client-3",
        eventType = EventType.DEATH_NOTIFICATION,
      ),
    )

    every { suppliersService.getSubscriptionsForSupplier(supplierId) }.returns(supplierSubscriptions)

    val supplierSubscriptionsOutput = underTest.getSubscriptionsForSupplier(supplierId).toList()

    assertThat(supplierSubscriptionsOutput).hasSize(3)
    assertThat(supplierSubscriptionsOutput).isEqualTo(supplierSubscriptions.toList())
  }

  @Test
  fun `addSupplierSubscription adds supplier subscription`() {
    val supplierId = UUID.randomUUID()
    val supplierSubscriptionRequest = SupplierSubRequest(
      clientId = "Client-New",
      eventType = EventType.LIFE_EVENT,
    )
    val supplierSubscription = SupplierSubscription(
      supplierId = supplierId,
      clientId = "Client-New",
      eventType = EventType.LIFE_EVENT,
    )

    every { suppliersService.addSupplierSubscription(supplierId, any()) }.returns(supplierSubscription)

    val supplierSubscriptionOutput = underTest.addSupplierSubscription(supplierId, supplierSubscriptionRequest)

    assertThat(supplierSubscriptionOutput).isEqualTo(supplierSubscription)
  }

  @Test
  fun `updateSupplierSubscription updates supplier subscription`() {
    val supplierId = UUID.randomUUID()
    val subscriptionId = UUID.randomUUID()
    val supplierSubscriptionRequest = SupplierSubRequest(
      clientId = "Client-New",
      eventType = EventType.LIFE_EVENT,
    )
    val supplierSubscription = SupplierSubscription(
      supplierId = supplierId,
      id = subscriptionId,
      clientId = "Client-New",
      eventType = EventType.LIFE_EVENT,
    )

    every { suppliersService.updateSupplierSubscription(supplierId, subscriptionId, any()) }.returns(
      supplierSubscription,
    )

    val supplierSubscriptionOutput =
      underTest.updateSupplierSubscription(supplierId, subscriptionId, supplierSubscriptionRequest)

    assertThat(supplierSubscriptionOutput).isEqualTo(supplierSubscription)
  }

  @Test
  fun `deleteSupplierSubscription deletes supplier subscription`() {
    val subscriptionId = UUID.randomUUID()
    val supplierSubscription = SupplierSubscription(
      supplierId = UUID.randomUUID(),
      id = subscriptionId,
      clientId = "Client-New",
      eventType = EventType.LIFE_EVENT,
    )

    every { suppliersService.deleteSupplierSubscription(subscriptionId) }.returns(
      supplierSubscription,
    )

    val supplierSubscriptionOutput = underTest.deleteSupplierSubscription(subscriptionId)

    assertThat(supplierSubscriptionOutput).isEqualTo(supplierSubscription)

    verify(exactly = 1) {
      suppliersService.deleteSupplierSubscription(subscriptionId)
    }
  }
}
