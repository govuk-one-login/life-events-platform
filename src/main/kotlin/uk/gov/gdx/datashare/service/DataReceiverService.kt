package uk.gov.gdx.datashare.service

import com.amazonaws.services.sqs.model.SendMessageRequest
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.config.AuthenticationFacade
import uk.gov.gdx.datashare.config.DateTimeHandler
import uk.gov.gdx.datashare.controller.EventToPublish
import uk.gov.gdx.datashare.queue.AwsQueue
import uk.gov.gdx.datashare.queue.AwsQueueService
import uk.gov.gdx.datashare.repository.*
import java.time.LocalDateTime
import java.util.*

@Service
class DataReceiverService(
  private val awsQueueService: AwsQueueService,
  private val authenticationFacade: AuthenticationFacade,
  private val publisherSubscriptionRepository: PublisherSubscriptionRepository,
  private val publisherRepository: PublisherRepository,
  private val eventDatasetRepository: EventDatasetRepository,
  private val objectMapper: ObjectMapper,
  private val dateTimeHandler: DateTimeHandler,
  private val meterRegistry: MeterRegistry,
) {
  private val dataReceiverQueue by lazy { awsQueueService.findByQueueId("dataprocessor") as AwsQueue }
  private val dataReceiverSqsClient by lazy { dataReceiverQueue.sqsClient }
  private val dataReceiverQueueUrl by lazy { dataReceiverQueue.queueUrl }

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun sendToDataProcessor(eventPayload: EventToPublish) {
    // check if client is allowed to send
    val subscription = publisherSubscriptionRepository.findByClientIdAndEventType(
      authenticationFacade.getUsername(),
      eventPayload.eventType,
    ) ?: throw RuntimeException("${authenticationFacade.getUsername()} does not have permission")

    val dataSet = eventDatasetRepository.findById(subscription.datasetId)
      ?: throw RuntimeException("Client ${authenticationFacade.getUsername()} is not a known dataset")

    val publisher = publisherRepository.findById(subscription.publisherId)
      ?: throw RuntimeException("Client ${authenticationFacade.getUsername()} is not a known publisher")

    if (dataSet.storePayload && eventPayload.eventDetails == null) {
      throw RuntimeException("Client ${authenticationFacade.getUsername()} must publish dataset for this event in format ${subscription.datasetId}")
    }

    val dataProcessorMessage = DataProcessorMessage(
      subscriptionId = subscription.id,
      datasetId = subscription.datasetId,
      publisher = publisher.name,
      eventTypeId = eventPayload.eventType,
      eventTime = eventPayload.eventTime ?: dateTimeHandler.now(),
      id = eventPayload.id,
      storePayload = dataSet.storePayload,
      details = if (dataSet.storePayload) {
        eventPayload.eventDetails
      } else {
        null
      },
    )

    log.debug(
      "Notifying Data Processor of event type {} from {}",
      dataProcessorMessage.eventTypeId,
      publisher.name,
    )

    meterRegistry.counter("EVENT_ACTION.IngressEventPublished", "eventType", eventPayload.eventType).increment()

    dataReceiverSqsClient.sendMessage(
      SendMessageRequest(
        dataReceiverQueueUrl,
        dataProcessorMessage.toJson(),
      ),
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
  val subscriptionId: UUID,
  val id: String?,
  val details: String?,
)
