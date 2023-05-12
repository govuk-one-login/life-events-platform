package uk.gov.gdx.datashare.services

import net.minidev.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.gdx.datashare.config.NoDataFoundException
import uk.gov.gdx.datashare.repositories.SupplierEventRepository
import java.time.LocalDateTime
import java.util.*

@Service
class EventConsumedCheckingService(
  private val lambdaService: LambdaService,
  private val supplierEventRepository: SupplierEventRepository,
  private val objectMapper: ObjectMapper,
  @Value("\${delete.event.lambda.function.name}") val functionName: String,
) {

  @Transactional
  fun checkAndMarkConsumed() {
    // Check is all consumed or over a month elapsed - need to make it configurable obs!
    supplierEventRepository.findAllByCreatedAtBeforeAndEventConsumedIsFalse(LocalDateTime.now().minusMonths(1))
      .map { it.id }.plus(
        supplierEventRepository.findAllByDeletedEventsForAllAcquirers(),
      )
      .forEach {
        deleteEvent(it) // TODO: Will need to handle transactions on failures etc
        supplierEventRepository.markAsFullyConsumed(it)
      }
  }

  private fun deleteEvent(id: UUID): DeleteEventResponse {
    val jsonPayload = createDeleteEventPayload(id)

    val res = lambdaService.invokeLambda(functionName, jsonPayload)
    val parsedResponse = lambdaService.parseLambdaResponse(res, DeleteEventResponse::class.java)

    if (parsedResponse.statusCode == HttpStatus.NOT_FOUND.value()) {
      throw NoDataFoundException(id.toString())
    }

    return parsedResponse
  }

  private fun createDeleteEventPayload(dataId: UUID): String {
    val jsonObj = JSONObject()
    jsonObj["id"] = dataId.toString()
    return jsonObj.toString()
  }
}

data class DeleteEventResponse(
  val id: String,
  val statusCode: Int,
)
