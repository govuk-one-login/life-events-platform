package uk.gov.gdx.datashare.services

import com.amazonaws.xray.spring.aop.XRayEnabled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.gdx.datashare.enums.CognitoClientType
import uk.gov.gdx.datashare.models.*
import uk.gov.gdx.datashare.repositories.Acquirer

@Service
@XRayEnabled
class AdminService(
  private val cognitoService: CognitoService,
  private val acquirersService: AcquirersService,
  private val suppliersService: SuppliersService,
  private val adminActionAlertsService: AdminActionAlertsService,
) {
  @Transactional
  fun createAcquirer(createAcquirerRequest: CreateAcquirerRequest): CreateAcquirerResponse {
    adminActionAlertsService.noticeAction(AdminAction("Create Acquirer", createAcquirerRequest))
    val acquirer = acquirersService.addAcquirer(AcquirerRequest(createAcquirerRequest.acquirerName))

    if (createAcquirerRequest.queueName == null) {
      return createApiAcquirer(createAcquirerRequest, acquirer)
    }

    return createQueueAcquirer(acquirer, createAcquirerRequest)
  }

  private fun createApiAcquirer(
    createAcquirerRequest: CreateAcquirerRequest,
    acquirer: Acquirer,
  ): CreateAcquirerResponse {
    return cognitoService.createUserPoolClient(
      CognitoClientRequest(createAcquirerRequest.acquirerName, listOf(CognitoClientType.ACQUIRER)),
    ).let {
      acquirersService.addAcquirerSubscription(
        acquirer.id,
        AcquirerSubRequest(
          oauthClientId = it.clientId,
          eventType = createAcquirerRequest.eventType,
          enrichmentFields = createAcquirerRequest.enrichmentFields,
          enrichmentFieldsIncludedInPoll = createAcquirerRequest.enrichmentFieldsIncludedInPoll,
        ),
      )

      CreateAcquirerResponse(
        queueUrl = null,
        clientName = it.clientName,
        clientId = it.clientId,
        clientSecret = it.clientSecret,
      )
    }
  }

  private fun createQueueAcquirer(
    acquirer: Acquirer,
    createAcquirerRequest: CreateAcquirerRequest,
  ): CreateAcquirerResponse {
    return acquirersService.addAcquirerSubscription(
      acquirer.id,
      AcquirerSubRequest(
        oauthClientId = null,
        eventType = createAcquirerRequest.eventType,
        enrichmentFields = createAcquirerRequest.enrichmentFields,
        enrichmentFieldsIncludedInPoll = createAcquirerRequest.enrichmentFieldsIncludedInPoll,
        queueName = createAcquirerRequest.queueName,
        principalArn = createAcquirerRequest.principalArn,
      ),
    ).let {
      CreateAcquirerResponse(
        queueUrl = it.queueUrl,
        clientId = null,
        clientName = null,
        clientSecret = null,
      )
    }
  }

  @Transactional
  fun createSupplier(createSupplierRequest: CreateSupplierRequest): CognitoClientResponse? {
    adminActionAlertsService.noticeAction(AdminAction("Create Supplier", createSupplierRequest))
    val supplier = suppliersService.addSupplier(SupplierRequest(createSupplierRequest.clientName))
    return cognitoService.createUserPoolClient(
      CognitoClientRequest(createSupplierRequest.clientName, listOf(CognitoClientType.SUPPLIER)),
    ).let {
      suppliersService.addSupplierSubscription(
        supplier.supplierId,
        SupplierSubRequest(
          clientId = it.clientId,
          eventType = createSupplierRequest.eventType,
        ),
      )

      CognitoClientResponse(it.clientName, it.clientId, it.clientSecret)
    }
  }
}
