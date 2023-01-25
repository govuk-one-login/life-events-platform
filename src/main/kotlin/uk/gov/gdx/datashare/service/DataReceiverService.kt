package uk.gov.gdx.datashare.service

import com.amazonaws.services.sqs.model.SendMessageRequest
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.config.*
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

  fun sendToDataProcessor(eventPayload: EventToPublish) {
    // check if client is allowed to send
    val subscription = publisherSubscriptionRepository.findByClientIdAndEventType(
      authenticationFacade.getUsername(),
      eventPayload.eventType,
    ) ?: throw PublisherPermissionException("${authenticationFacade.getUsername()} does not have permission")

    val publisher = publisherRepository.findByIdOrNull(subscription.publisherId)
      ?: throw PublisherSubscriptionNotFoundException("Client ${authenticationFacade.getUsername()} is not a known publisher")

    val dataProcessorMessage = DataProcessorMessage(
      subscriptionId = subscription.id,
      datasetId = subscription.datasetId,
      publisher = publisher.name,
      eventTypeId = eventPayload.eventType,
      eventTime = eventPayload.eventTime ?: dateTimeHandler.now(),
      id = eventPayload.id,
    )

    log.debug(
      "Notifying Data Processor of event type {} from {}",
      dataProcessorMessage.eventTypeId,
      publisher.name,
    )

    meterRegistry.counter("EVENT_ACTION.EventPublished", "eventType", eventPayload.eventType).increment()

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
  val subscriptionId: UUID,
  val id: String?,
)
