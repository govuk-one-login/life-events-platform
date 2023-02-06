package uk.gov.gdx.datashare.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParameterRequest

@Service
@ConditionalOnProperty(name = ["ssm-client.enabled"], havingValue = "true")
class SsmClientService(
  @Value("\${environment:-}") private val environment: String,
) {
  private val ssmClient = SsmClient.create()

  fun getParameter(parameterName: String): String {
    return getParameterFromSsm("$environment-$parameterName")
  }

  private fun getParameterFromSsm(parameterName: String): String {
    val paramRequest: GetParameterRequest = GetParameterRequest.builder().name(parameterName).build()
    val paramResponse = ssmClient.getParameter(paramRequest)
    return paramResponse.parameter().value()
  }
}
