package uk.gov.gdx.datashare.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.InvokeRequest
import software.amazon.awssdk.services.lambda.model.InvokeResponse

@Service
class LambdaService(
  private val objectMapper: ObjectMapper,
) {
  private val lambdaClient by lazy { LambdaClient.builder().build() }

  fun invokeLambda(functionName: String, jsonPayload: String): InvokeResponse {
    val payload = SdkBytes.fromUtf8String(jsonPayload)
    val invokeRequest = createInvokeRequest(functionName, payload)

    return lambdaClient.invoke(invokeRequest)
  }

  fun <T> parseLambdaResponse(response: InvokeResponse, valueType: Class<T>): T {
    val value: String = response.payload().asUtf8String()
    return objectMapper.readValue(value, valueType)
  }

  private fun createInvokeRequest(functionName: String, payload: SdkBytes): InvokeRequest =
    InvokeRequest.builder()
      .functionName(functionName)
      .payload(payload)
      .build()
}
