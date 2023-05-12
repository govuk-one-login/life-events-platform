package uk.gov.gdx.datashare.services

import net.minidev.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.config.NoDataFoundException
import uk.gov.gdx.datashare.models.GroDeathRecord
import uk.gov.gdx.datashare.models.GroEnrichEventResponse

@Service
class GroApiService(
  private val lambdaService: LambdaService,
  @Value("\${environment}") val environment: String,
) {
  fun enrichEvent(dataId: String): GroDeathRecord {
    val jsonPayload = createEnrichEventPayload(dataId)

    val res = lambdaService.invokeLambda(functionName, jsonPayload)
    val parsedResponse = lambdaService.parseLambdaResponse(res, GroEnrichEventResponse::class.java)

    if (parsedResponse.StatusCode.equals(HttpStatus.NOT_FOUND)) {
      throw NoDataFoundException(dataId)
    }

    return parsedResponse.Event!!
  }

  private fun createEnrichEventPayload(dataId: String): String {
    val jsonObj = JSONObject()
    jsonObj["id"] = dataId
    return jsonObj.toString()
  }
}
