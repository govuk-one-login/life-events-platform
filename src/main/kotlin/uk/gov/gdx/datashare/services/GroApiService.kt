package uk.gov.gdx.datashare.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.config.NoDataFoundException
import uk.gov.gdx.datashare.models.GroDeathRecord
import uk.gov.gdx.datashare.models.GroEnrichEventResponse

@Service
class GroApiService(
  private val lambdaService: LambdaService,
  private val objectMapper: ObjectMapper,
  @Value("\${enrich.event.lambda.function.name}") val functionName: String,
) {
  fun enrichEvent(dataId: String): GroDeathRecord {
    val jsonPayload = objectMapper.writeValueAsString(
      object {
        val id = dataId
      },
    )

    val res = lambdaService.invokeLambda(functionName, jsonPayload)
    val parsedResponse = lambdaService.parseLambdaResponse(res, GroEnrichEventResponse::class.java)

    if (parsedResponse.StatusCode.equals(HttpStatus.NOT_FOUND) || parsedResponse.Event == null) {
      throw NoDataFoundException(dataId)
    }

    return parsedResponse.Event
  }
}
