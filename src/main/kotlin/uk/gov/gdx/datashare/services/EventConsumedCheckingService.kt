package uk.gov.gdx.datashare.services

import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.invoke.LambdaFunction
import com.amazonaws.services.lambda.invoke.LambdaInvokerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.gdx.datashare.repositories.AcquirerEventRepository
import uk.gov.gdx.datashare.repositories.SupplierEventRepository
import java.time.LocalDateTime
import java.util.*

@Service
class EventConsumedCheckingService(
  private val acquirerEventRepository: AcquirerEventRepository,
  private val supplierEventRepository: SupplierEventRepository,
) {

  @Transactional
  fun checkAndMarkConsumed() {
    supplierEventRepository.findAllByCreatedAtBeforeAndEventConsumedIsFalse(LocalDateTime.now().minusMonths(1))
      .map { it.id }.plus(
        acquirerEventRepository.findAllByDeletedEventsForAllAcquirers(),
      )
      .forEach {
        deleteEvent(it) // TODO: Will need to handle transactions on failures etc
        supplierEventRepository.markAsFullyConsumed(it)
      }
  }

  private fun deleteEvent(id: UUID): DeleteEventResponse {
    val client = AWSLambdaClientBuilder.defaultClient()

    val deleteEvent = LambdaInvokerFactory.builder()
      .lambdaClient(client)
      .build(DeleteEventService::class.java)

    return deleteEvent.deleteEvent(EventRequest(id.toString()))
  }
}

data class EventRequest(
  val id: String,
)

data class DeleteEventResponse(
  val id: String,
  val statusCode: Int,
)

interface DeleteEventService {
  // TODO: This is just placeholder for agreed implementation
  @LambdaFunction(functionName = "dev-gro-ingestion-lambda-function-delete-event")
  fun deleteEvent(input: EventRequest): DeleteEventResponse
}
