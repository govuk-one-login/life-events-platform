package uk.gov.gdx.datashare.services

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.gdx.datashare.enums.CognitoClientType
import uk.gov.gdx.datashare.models.*

@Service
class AdminService(
  private val cognitoService: CognitoService,
  private val acquirersService: AcquirersService,
) {
  fun createCognitoClient(cognitoClientRequest: CognitoClientRequest) =
    cognitoService.createUserPoolClient(cognitoClientRequest)

  @Transactional
  fun createAcquirer(createAcquirerRequest: CreateAcquirerRequest): CognitoClientResponse? {
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
}
