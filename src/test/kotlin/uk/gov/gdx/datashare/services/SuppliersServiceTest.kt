package uk.gov.gdx.datashare.services

import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import uk.gov.gdx.datashare.config.DateTimeHandler
import uk.gov.gdx.datashare.config.SupplierSubscriptionNotFoundException
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.models.SupplierRequest
import uk.gov.gdx.datashare.models.SupplierSubRequest
import uk.gov.gdx.datashare.repositories.Supplier
import uk.gov.gdx.datashare.repositories.SupplierRepository
import uk.gov.gdx.datashare.repositories.SupplierSubscription
import uk.gov.gdx.datashare.repositories.SupplierSubscriptionRepository
import java.time.LocalDateTime
import java.util.*

class SuppliersServiceTest {
  private val adminActionAlertsService = mockk<AdminActionAlertsService>()
  private val cognitoService = mockk<CognitoService>()
  private val dateTimeHandler = mockk<DateTimeHandler>()
  private val supplierSubscriptionRepository = mockk<SupplierSubscriptionRepository>()
  private val supplierRepository = mockk<SupplierRepository>()

  private val underTest = SuppliersService(
    adminActionAlertsService,
    cognitoService,
    dateTimeHandler,
    supplierRepository,
    supplierSubscriptionRepository,
  )

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
    every { adminActionAlertsService.noticeAction(any()) } just runs
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
    every { adminActionAlertsService.noticeAction(any()) } just runs

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
    every { adminActionAlertsService.noticeAction(any()) } just runs
    val exception = assertThrows<SupplierSubscriptionNotFoundException> {
      underTest.updateSupplierSubscription(supplier.id, supplierSubscription.id, supplierSubRequest)
    }

    assertThat(exception.message).isEqualTo("Subscription ${supplierSubscription.id} not found")

    verify(exactly = 0) { supplierSubscriptionRepository.save(any()) }
  }

  @Test
  fun `deleteSupplierSubscription deletes subscription and cognito client`() {
    val now = LocalDateTime.now()
    every { dateTimeHandler.now() }.returns(now)

    every { supplierSubscriptionRepository.findByIdOrNull(supplierSubscription.id) }.returns(supplierSubscription)
    every { supplierSubscriptionRepository.findAllByClientId(supplierSubscription.clientId) }.returns(
      emptyList(),
    )
    every { supplierSubscriptionRepository.save(any()) }.returns(supplierSubscription)
    every { adminActionAlertsService.noticeAction(any()) } just runs
    every { cognitoService.deleteUserPoolClient(supplierSubscription.clientId) } just runs

    underTest.deleteSupplierSubscription(supplierSubscription.id)

    verify(exactly = 1) {
      supplierSubscriptionRepository.save(
        withArg {
          assertThat(it.whenDeleted).isEqualTo(now)
        },
      )
    }

    verify(exactly = 1) {
      cognitoService.deleteUserPoolClient(supplierSubscription.clientId)
    }
  }

  @Test
  fun `deleteSupplierSubscription deletes subscription and not cognito client if shared`() {
    val now = LocalDateTime.now()
    every { dateTimeHandler.now() }.returns(now)

    every { supplierSubscriptionRepository.findByIdOrNull(supplierSubscription.id) }.returns(supplierSubscription)
    every { supplierSubscriptionRepository.findAllByClientId(supplierSubscription.clientId) }.returns(
      listOf(otherSupplierSubscription),
    )
    every { supplierSubscriptionRepository.save(any()) }.returns(supplierSubscription)
    every { adminActionAlertsService.noticeAction(any()) } just runs
    every { cognitoService.deleteUserPoolClient(supplierSubscription.clientId) } just runs

    underTest.deleteSupplierSubscription(supplierSubscription.id)

    verify(exactly = 1) {
      supplierSubscriptionRepository.save(
        withArg {
          assertThat(it.whenDeleted).isEqualTo(now)
        },
      )
    }

    verify(exactly = 0) {
      cognitoService.deleteUserPoolClient(supplierSubscription.clientId)
    }
  }

  @Test
  fun `deleteSupplierSubscription does not delete subscription if subscription does not exist`() {
    val now = LocalDateTime.now()
    every { dateTimeHandler.now() }.returns(now)

    every { supplierSubscriptionRepository.findByIdOrNull(supplierSubscription.id) }.returns(null)
    every { adminActionAlertsService.noticeAction(any()) } just runs

    val exception = assertThrows<SupplierSubscriptionNotFoundException> {
      underTest.deleteSupplierSubscription(supplierSubscription.id)
    }

    assertThat(exception.message).isEqualTo("Subscription ${supplierSubscription.id} not found")

    verify(exactly = 0) { supplierSubscriptionRepository.save(any()) }

    verify(exactly = 0) { cognitoService.deleteUserPoolClient(any()) }
  }

  @Test
  fun `addSupplier adds supplier`() {
    val supplierRequest = SupplierRequest(
      name = "Supplier",
    )

    every { supplierRepository.save(any()) }.returns(supplier)
    every { adminActionAlertsService.noticeAction(any()) } just runs

    underTest.addSupplier(supplierRequest)

    verify(exactly = 1) {
      supplierRepository.save(
        withArg {
          assertThat(it.name).isEqualTo(supplierRequest.name)
        },
      )
    }
  }

  @Test
  fun `addSupplier action is noticed`() {
    val supplierRequest = SupplierRequest(
      name = "Supplier",
    )

    every { supplierRepository.save(any()) }.returns(supplier)
    every { adminActionAlertsService.noticeAction(any()) } just runs

    underTest.addSupplier(supplierRequest)

    verify(exactly = 1) {
      adminActionAlertsService.noticeAction(
        withArg {
          assertThat(it.name).isEqualTo("Add supplier")
          assertThat(it.details).isEqualTo(supplierRequest)
        },
      )
    }
  }

  @Test
  fun `updateSupplierSubscription action is noticed`() {
    every { supplierSubscriptionRepository.findByIdOrNull(supplierSubscription.id) }.returns(supplierSubscription)
    every { supplierSubscriptionRepository.save(any()) }.returns(supplierSubscription)
    every { adminActionAlertsService.noticeAction(any()) } just runs

    underTest.updateSupplierSubscription(supplier.id, supplierSubscription.id, supplierSubRequest)

    verify(exactly = 1) {
      adminActionAlertsService.noticeAction(
        withArg {
          assertThat(it.name).isEqualTo("Update supplier subscription")
          assertThat(it.details.getProperty("supplierId")).isEqualTo(supplier.id)
          assertThat(it.details.getProperty("subscriptionId")).isEqualTo(supplierSubscription.id)
          assertThat(it.details.getProperty("supplierSubRequest")).isEqualTo(supplierSubRequest)
        },
      )
    }
  }

  @Test
  fun `addSupplierSubscription action is noticed`() {
    every { supplierSubscriptionRepository.save(any()) }.returns(supplierSubscription)
    every { adminActionAlertsService.noticeAction(any()) } just runs
    underTest.addSupplierSubscription(supplier.id, supplierSubRequest)

    verify(exactly = 1) {
      adminActionAlertsService.noticeAction(
        withArg {
          assertThat(it.name).isEqualTo("Add supplier subscription")
          assertThat(it.details.getProperty("supplierId")).isEqualTo(supplier.id)
          assertThat(it.details.getProperty("supplierSubRequest")).isEqualTo(supplierSubRequest)
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
  private val otherSupplierSubscription = SupplierSubscription(
    supplierId = UUID.randomUUID(),
    clientId = "Client2",
    eventType = EventType.DEATH_NOTIFICATION,
  )
  private val supplierSubRequest = SupplierSubRequest(
    clientId = "Client-New",
    eventType = EventType.LIFE_EVENT,
  )
}
