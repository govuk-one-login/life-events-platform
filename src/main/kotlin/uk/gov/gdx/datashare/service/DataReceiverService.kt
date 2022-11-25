package uk.gov.gdx.datashare.service

import com.amazonaws.services.sqs.model.SendMessageRequest
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.config.AuthenticationFacade
import uk.gov.gdx.datashare.repository.DataProviderRepository
import uk.gov.gdx.datashare.resource.ApiEventPayload
import uk.gov.gdx.datashare.resource.EventType
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.time.LocalDateTime
import java.util.*


@Service
class DataReceiverService(
  private val hmppsQueueService: HmppsQueueService,
  private val authenticationFacade: AuthenticationFacade,
  private val dataProviderRepository: DataProviderRepository,
  private val objectMapper: ObjectMapper,
) {
  private val dataReceiverQueue by lazy { hmppsQueueService.findByQueueId("dataprocessor") as HmppsQueue }
  private val dataReceiverSqsClient by lazy { dataReceiverQueue.sqsClient }
  private val dataReceiverQueueUrl by lazy { dataReceiverQueue.queueUrl }

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun sendToDataProcessor(eventPayload : ApiEventPayload) {

    // check if client is allowed to send
    val dataProvider = dataProviderRepository.findById(authenticationFacade.getUsername())
      ?: throw RuntimeException("Client ${authenticationFacade.getUsername()} is not a known client")

    // check if client is allowed to send this type of event
    if (dataProvider.eventType != eventPayload.eventType.toString()) {
      throw RuntimeException("Client ${dataProvider.clientName} is not allowed to provide ${eventPayload.eventType} events")
    }

    val dataProcessorMessage = DataProcessorMessage(
      dataSetType = dataProvider.datasetType,
      eventType = eventPayload.eventType,
      eventTime = eventPayload.eventTime?: LocalDateTime.now(),
      provider = dataProvider.clientName,
      storePayload = dataProvider.storePayload,
      id = eventPayload.id,
      details = eventPayload.eventDetails,
    )

    log.debug("Notifying Data Processor of event type {} from {}", dataProcessorMessage.eventType, dataProcessorMessage.provider)

    dataReceiverSqsClient.sendMessage(
      SendMessageRequest(
        dataReceiverQueueUrl,
        dataProcessorMessage.toJson()
      )
    )
  }

  private fun Any.toJson() = objectMapper.writeValueAsString(this)
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DataProcessorMessage(
  val dataSetType: String,
  val provider: String,
  val storePayload: Boolean = false,
  val eventType: EventType,
  val eventTime: LocalDateTime,
  val id: String?,
  val details: String?,
)

