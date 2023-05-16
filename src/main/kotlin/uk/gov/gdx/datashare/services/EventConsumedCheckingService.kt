package uk.gov.gdx.datashare.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.config.DateTimeHandler
import uk.gov.gdx.datashare.config.NoDataFoundException
import uk.gov.gdx.datashare.repositories.SupplierEventRepository
import java.util.*

@Service
class EventConsumedCheckingService(
  private val dateTimeHandler: DateTimeHandler,
  private val lambdaService: LambdaService,
  private val objectMapper: ObjectMapper,
  private val supplierEventRepository: SupplierEventRepository,
  @Value("\${delete.event.lambda.function.name:#{null}}") val functionName: String?,
) {
  fun checkAndMarkConsumed() {
    val now = dateTimeHandler.now()
    supplierEventRepository.findGroDeathEventsForDeletion()
      .forEach {
        deleteEvent(it.id)
        supplierEventRepository.save(it.copy(deletedAt = now))
      }
  }

  private fun deleteEvent(id: UUID): DeleteEventResponse {
    val deleteEventFunctionName = functionName ?: throw IllegalStateException("Function name not found.")

    val jsonPayload = objectMapper.writeValueAsString(
      object {
        val id = id
      },
    )

    val res = lambdaService.invokeLambda(deleteEventFunctionName, jsonPayload)
    val parsedResponse = lambdaService.parseLambdaResponse(res, DeleteEventResponse::class.java)

    if (parsedResponse.statusCode == HttpStatus.NOT_FOUND.value()) {
      throw NoDataFoundException(id.toString())
    }

    return parsedResponse
  }
}

data class DeleteEventResponse(
  val id: String,
  val statusCode: Int,
)
