package uk.gov.gdx.datashare.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.gdx.datashare.config.DateTimeHandler
import uk.gov.gdx.datashare.config.NoDataFoundException
import uk.gov.gdx.datashare.models.GroDeathRecord
import uk.gov.gdx.datashare.models.GroDeleteEventResponse
import uk.gov.gdx.datashare.models.GroEnrichEventResponse
import uk.gov.gdx.datashare.repositories.SupplierEvent
import uk.gov.gdx.datashare.repositories.SupplierEventRepository
import java.util.*

@Service
class GroApiService(
  private val dateTimeHandler: DateTimeHandler,
  private val lambdaService: LambdaService,
  private val objectMapper: ObjectMapper,
  private val supplierEventRepository: SupplierEventRepository,
  @Value("\${delete.event.lambda.function.name:#{null}}") val deleteFunctionName: String?,
  @Value("\${enrich-event-lambda-function-name:#{null}}") val enrichFunctionName: String?,
) {
  @Transactional
  fun deleteConsumedGroSupplierEvent(event: SupplierEvent) {
    deleteEvent(event.dataId)
    supplierEventRepository.save(event.copy(deletedAt = dateTimeHandler.now()))
  }

  fun enrichEvent(dataId: String): GroDeathRecord {
    val enrichEventFunctionName = enrichFunctionName ?: throw IllegalStateException("Function name for enrich not found.")

    val jsonPayload = objectMapper.writeValueAsString(
      object {
        val id = dataId
      },
    )

    val res = lambdaService.invokeLambda(enrichEventFunctionName, jsonPayload)
    val parsedResponse = lambdaService.parseLambdaResponse(res, GroEnrichEventResponse::class.java)

    if (parsedResponse.statusCode == HttpStatus.NOT_FOUND.value() || parsedResponse.payload == null) {
      throw NoDataFoundException("No data found to enrich ID $dataId")
    }

    return parsedResponse.payload
  }

  private fun deleteEvent(dataId: String): String {
    val deleteEventFunctionName = deleteFunctionName ?: throw IllegalStateException("Function name for delete not found.")

    val jsonPayload = objectMapper.writeValueAsString(
      object {
        val id = dataId
      },
    )

    val res = lambdaService.invokeLambda(deleteEventFunctionName, jsonPayload)
    return lambdaService.parseLambdaResponse(res, GroDeleteEventResponse::class.java).payload
  }
}
