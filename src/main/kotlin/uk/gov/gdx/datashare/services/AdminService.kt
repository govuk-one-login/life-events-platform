package uk.gov.gdx.datashare.services

import com.amazonaws.xray.spring.aop.XRayEnabled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.gdx.datashare.enums.CognitoClientType
import uk.gov.gdx.datashare.models.*

@Service
@XRayEnabled
class AdminService(
  private val cognitoService: CognitoService,
  private val acquirersService: AcquirersService,
  private val suppliersService: SuppliersService,
  private val adminActionAlertsService: AdminActionAlertsService,
) {
  @Transactional
  fun createAcquirer(createAcquirerRequest: CreateAcquirerRequest): CognitoClientResponse? {
    adminActionAlertsService.noticeAction(AdminAction("Create Acquirer", createAcquirerRequest))
    val acquirer = acquirersService.addAcquirer(AcquirerRequest(createAcquirerRequest.clientName))
    return cognitoService.createUserPoolClient(
      CognitoClientRequest(createAcquirerRequest.clientName, listOf(CognitoClientType.ACQUIRER)),
    )?.let {
      acquirersService.addAcquirerSubscription(
        acquirer.id,
        AcquirerSubRequest(
          oauthClientId = it.clientId,
          eventType = createAcquirerRequest.eventType,
          enrichmentFields = createAcquirerRequest.enrichmentFields,
          enrichmentFieldsIncludedInPoll = createAcquirerRequest.enrichmentFieldsIncludedInPoll,
        ),
      )

      CognitoClientResponse(it.clientName, it.clientId, it.clientSecret)
    }
  }

  @Transactional
  fun createSupplier(createSupplierRequest: CreateSupplierRequest): CognitoClientResponse? {
    adminActionAlertsService.noticeAction(AdminAction("Create Supplier", createSupplierRequest))
    val supplier = suppliersService.addSupplier(SupplierRequest(createSupplierRequest.clientName))
    return cognitoService.createUserPoolClient(
      CognitoClientRequest(createSupplierRequest.clientName, listOf(CognitoClientType.SUPPLIER)),
    )?.let {
      suppliersService.addSupplierSubscription(
        supplier.id,
        SupplierSubRequest(
          clientId = it.clientId,
          eventType = createSupplierRequest.eventType,
        ),
      )

      CognitoClientResponse(it.clientName, it.clientId, it.clientSecret)
    }
  }
}
