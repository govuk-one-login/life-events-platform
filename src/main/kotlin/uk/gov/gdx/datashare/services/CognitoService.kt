package uk.gov.gdx.datashare.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolClientRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.ExplicitAuthFlowsType
import software.amazon.awssdk.services.cognitoidentityprovider.model.OAuthFlowType
import software.amazon.awssdk.services.cognitoidentityprovider.model.TokenValidityUnitsType
import uk.gov.gdx.datashare.enums.CognitoClientType
import uk.gov.gdx.datashare.models.CognitoClientRequest
import uk.gov.gdx.datashare.models.CognitoClientResponse

@Service
class CognitoService(
  @Value("\${cognito.user-pool-id}") val userPoolId: String,
  @Value("\${cognito.enabled}") val enabled: Boolean,
  @Value("\${cognito.acquirer-scope}") val acquirerScope: String,
  @Value("\${cognito.supplier-scope}") val supplierScope: String,
  @Value("\${cognito.admin-scope}") val adminScope: String,
) {
  fun createUserPoolClient(cognitoClientRequest: CognitoClientRequest): CognitoClientResponse? {
    val scopes = cognitoClientRequest.clientTypes.map {
      when (it) {
        CognitoClientType.ACQUIRER -> acquirerScope
        CognitoClientType.SUPPLIER -> supplierScope
        CognitoClientType.ADMIN -> adminScope
      }
    }

    val userPoolClientRequest = baseUserPoolClientRequestBuilder
      .clientName(cognitoClientRequest.clientName)
      .userPoolId(userPoolId)

    scopes.forEach {
      userPoolClientRequest.allowedOAuthScopes(it)
    }

    val response = createCognitoClient()
      .createUserPoolClient(userPoolClientRequest.build())
      .userPoolClient()

    return response?.let {
      CognitoClientResponse(
        clientName = response.clientName(),
        clientId = response.clientId(),
        clientSecret = response.clientSecret(),
      )
    }
  }

  private fun createCognitoClient() =
    if (enabled) {
      CognitoIdentityProviderClient.builder().build()
    } else {
      throw IllegalStateException("Cognito integration disabled")
    }

  // This must be kept in line with the terraform simple_user_pool_client/main.tf
  private val baseUserPoolClientRequestBuilder = CreateUserPoolClientRequest
    .builder()
    .allowedOAuthFlows(OAuthFlowType.CLIENT_CREDENTIALS)
    .allowedOAuthFlowsUserPoolClient(true)
    .accessTokenValidity(60)
    .idTokenValidity(60)
    .generateSecret(true)
    .explicitAuthFlows(ExplicitAuthFlowsType.ALLOW_USER_PASSWORD_AUTH, ExplicitAuthFlowsType.ALLOW_REFRESH_TOKEN_AUTH)
    .preventUserExistenceErrors("ENABLED")
    .enableTokenRevocation(false)
    .tokenValidityUnits(
      TokenValidityUnitsType
        .builder()
        .accessToken("minutes")
        .idToken("minutes")
        .refreshToken("days")
        .build(),
    )
}
