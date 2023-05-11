package uk.gov.gdx.datashare.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.InvokeRequest
import software.amazon.awssdk.services.lambda.model.InvokeResponse

@Service
class LambdaService(
  private val objectMapper: ObjectMapper,
) {
  fun invokeLambda(functionName: String, jsonPayload: String): InvokeResponse {
    val payload = SdkBytes.fromUtf8String(jsonPayload)

    val lambdaClient = createLambdaClient()
    val invokeRequest = createInvokeRequest(functionName, payload)

    return lambdaClient.invoke(invokeRequest)
  }

  fun <T> parseLambdaResponse(response: InvokeResponse, valueType: Class<T>): T {
    val value: String = response.payload().asUtf8String()
    return objectMapper.readValue(value, valueType)
  }

  private fun createLambdaClient(): LambdaClient =
    LambdaClient.builder()
      .region(Region.EU_WEST_2)
      .build()

  private fun createInvokeRequest(functionName: String, payload: SdkBytes): InvokeRequest =
    InvokeRequest.builder()
      .functionName(functionName)
      .payload(payload)
      .build()
}
