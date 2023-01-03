package uk.gov.gdx.datashare.service

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import uk.gov.gdx.datashare.repository.ConsumerRepository
import uk.gov.gdx.datashare.repository.ConsumerSubscriptionRepository
import uk.gov.gdx.datashare.resource.EventInformation
import java.time.OffsetDateTime
import java.util.UUID

@Service
class LegacyAdaptorOutbound(
  private val objectMapper: ObjectMapper,
  private val eventDataRetrievalApiWebClient: WebClient,
  private val auditService: AuditService,
  private val consumerSubscriptionRepository: ConsumerSubscriptionRepository,
  private val consumerRepository: ConsumerRepository,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @JmsListener(destination = "adaptor", containerFactory = "hmppsQueueContainerFactoryProxy")
  fun onPublishedEvent(eventMessage: String) = runBlocking {
    val (message, messageAttributes) = objectMapper.readValue(eventMessage, EventTopicMessage::class.java)
    val eventType = messageAttributes.eventType.Value
    log.info("Received message $message, type $eventType")

    val event = objectMapper.readValue(message, EventMessage::class.java)
    when (eventType) {
      "DEATH_NOTIFICATION", "LIFE_EVENT" -> processLifeEvent(event)
      else -> {
        log.debug("Ignoring message with type $eventType")
      }
    }
  }

  suspend fun processLifeEvent(event: EventMessage) {
    log.debug("processing {}", event)

    val consumerSubscription = consumerSubscriptionRepository.findByEgressEventId(event.id)
    if (consumerSubscription?.isLegacy != true) {
      log.debug("Event {} is not legacy", event.id)
      return
    }

    // Go and get data from Event Retrieval API
    val lifeEvent = getEventPayload(event.id)

    // turn into file
    val details = lifeEvent.details
    if (details != null) {
      // who needs it?
      val clients = consumerSubscriptionRepository.findClientToSendDataTo(lifeEvent.eventType)
      clients.collect { client ->

        val consumer = consumerRepository.findById(client.consumerId)
          ?: throw RuntimeException("Consumer ${client.consumerId} not found")

        log.debug("Send to ${consumer.name} to ${client.pushUri} with [$details]")

        // Put code to send data here

        auditService.sendMessage(
          auditType = AuditType.PUSH_EVENT,
          id = event.id.toString(),
          details = "Push Event to ${consumer.name} : ${event.description}",
          username = consumer.name
        )
      }
    }
  }

  suspend fun getEventPayload(id: UUID): EventInformation =
    eventDataRetrievalApiWebClient.get()
      .uri("/event-data-retrieval/$id")
      .retrieve()
      .awaitBody()
}

data class GovEventType(val Value: String, val Type: String)
data class MessageAttributes(val eventType: GovEventType)
data class EventTopicMessage(
  val Message: String,
  val MessageAttributes: MessageAttributes
)

data class EventMessage(
  val id: UUID,
  val occurredAt: OffsetDateTime,
  val description: String
)
