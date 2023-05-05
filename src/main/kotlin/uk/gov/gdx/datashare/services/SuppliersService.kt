package uk.gov.gdx.datashare.services

import com.amazonaws.xray.spring.aop.XRayEnabled
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.gdx.datashare.config.SupplierSubscriptionNotFoundException
import uk.gov.gdx.datashare.models.SupplierRequest
import uk.gov.gdx.datashare.models.SupplierSubRequest
import uk.gov.gdx.datashare.repositories.Supplier
import uk.gov.gdx.datashare.repositories.SupplierRepository
import uk.gov.gdx.datashare.repositories.SupplierSubscription
import uk.gov.gdx.datashare.repositories.SupplierSubscriptionRepository
import java.util.*

@Service
@Transactional
@XRayEnabled
class SuppliersService(
  private val supplierSubscriptionRepository: SupplierSubscriptionRepository,
  private val supplierRepository: SupplierRepository,
  private val adminActionAlertsService: AdminActionAlertsService,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getSuppliers(): Iterable<Supplier> = supplierRepository.findAll()

  fun getSupplierSubscriptions(): Iterable<SupplierSubscription> = supplierSubscriptionRepository.findAll()

  fun getSubscriptionsForSupplier(supplierId: UUID) =
    supplierSubscriptionRepository.findAllBySupplierId(supplierId)

  fun addSupplierSubscription(
    supplierId: UUID,
    supplierSubRequest: SupplierSubRequest,
  ): SupplierSubscription {
    adminActionAlertsService.noticeAction(
      AdminAction(
        "Add supplier subscription",
        object {
          val supplierId = supplierId
          val supplierSubRequest = supplierSubRequest
        },
      ),
    )
    with(supplierSubRequest) {
      return supplierSubscriptionRepository.save(
        SupplierSubscription(
          supplierId = supplierId,
          clientId = clientId,
          eventType = eventType,
        ),
      )
    }
  }

  fun updateSupplierSubscription(
    supplierId: UUID,
    subscriptionId: UUID,
    supplierSubRequest: SupplierSubRequest,
  ): SupplierSubscription {
    adminActionAlertsService.noticeAction(
      AdminAction(
        "Update supplier subscription",
        object {
          val supplierId = supplierId
          val subscriptionId = subscriptionId
          val supplierSubRequest = supplierSubRequest
        },
      ),
    )
    with(supplierSubRequest) {
      return supplierSubscriptionRepository.save(
        supplierSubscriptionRepository.findByIdOrNull(subscriptionId)?.copy(
          supplierId = supplierId,
          clientId = clientId,
          eventType = eventType,
        ) ?: throw SupplierSubscriptionNotFoundException("Subscription $subscriptionId not found"),
      )
    }
  }

  fun addSupplier(
    supplierRequest: SupplierRequest,
  ): Supplier {
    adminActionAlertsService.noticeAction(AdminAction("Add supplier", supplierRequest))
    with(supplierRequest) {
      return supplierRepository.save(
        Supplier(
          name = name,
        ),
      )
    }
  }
}
