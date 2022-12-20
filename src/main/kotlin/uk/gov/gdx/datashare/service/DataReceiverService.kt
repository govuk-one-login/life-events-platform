package uk.gov.gdx.datashare.service

import com.amazonaws.services.sqs.model.SendMessageRequest
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.config.AuthenticationFacade
import uk.gov.gdx.datashare.repository.*
import uk.gov.gdx.datashare.resource.EventToPublish
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.time.LocalDateTime
import java.util.*

@Service
class DataReceiverService(
  private val hmppsQueueService: HmppsQueueService,
  private val authenticationFacade: AuthenticationFacade,
  private val publisherSubscriptionRepository: PublisherSubscriptionRepository,
  private val eventPublisherRepository: EventPublisherRepository,
  private val eventDatasetRepository: EventDatasetRepository,
  private val objectMapper: ObjectMapper,
) {
  private val dataReceiverQueue by lazy { hmppsQueueService.findByQueueId("dataprocessor") as HmppsQueue }
  private val dataReceiverSqsClient by lazy { dataReceiverQueue.sqsClient }
  private val dataReceiverQueueUrl by lazy { dataReceiverQueue.queueUrl }

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun sendToDataProcessor(eventPayload: EventToPublish) {

    // check if client is allowed to send
    val subscription = publisherSubscriptionRepository.findByClientIdAndEventType(
      authenticationFacade.getUsername(),
      eventPayload.eventType.toString()
    ) ?: throw RuntimeException("Publisher ${authenticationFacade.getUsername()} does not have a subscription for event type ${eventPayload.eventType}")

    val dataSet = eventDatasetRepository.findById(subscription.datasetId)
      ?: throw RuntimeException("Client ${authenticationFacade.getUsername()} is not a known dataset")

    val publisher = eventPublisherRepository.findById(subscription.publisherId)
      ?: throw RuntimeException("Client ${authenticationFacade.getUsername()} is not a known publisher")

    if (dataSet.storePayload && eventPayload.eventDetails == null) {
      throw RuntimeException("Client ${authenticationFacade.getUsername()} must publish dataset for this event in format ${subscription.datasetId}")
    }

    val dataProcessorMessage = DataProcessorMessage(
      subscriptionId = subscription.id,
      datasetId = subscription.datasetId,
      publisher = publisher.publisherName,
      eventTypeId = eventPayload.eventType,
      eventTime = eventPayload.eventTime ?: LocalDateTime.now(),
      id = eventPayload.id,
      storePayload = dataSet.storePayload,
      details = if (dataSet.storePayload) { eventPayload.eventDetails } else null
    )

    log.debug(
      "Notifying Data Processor of event type {} from {}",
      dataProcessorMessage.eventTypeId,
      publisher.publisherName
    )

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
  val datasetId: String,
  val eventTypeId: String,
  val eventTime: LocalDateTime,
  val publisher: String,
  val storePayload: Boolean = false,
  val subscriptionId: Long,
  val id: String?,
  val details: String?,
)
