package uk.gov.gdx.datashare.service

import com.amazonaws.services.s3.AmazonS3
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import uk.gov.gdx.datashare.config.S3Config
import uk.gov.gdx.datashare.controller.EventInformation
import java.time.OffsetDateTime
import java.util.*

@Service
class LegacyAdaptorOutbound(
  private val amazonS3: AmazonS3,
  private val s3Config: S3Config,
  private val objectMapper: ObjectMapper,
  private val eventDataRetrievalApiWebClient: WebClient,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @JmsListener(destination = "adaptor", containerFactory = "awsQueueContainerFactoryProxy")
  fun onPublishedEvent(eventMessage: String) = runBlocking {
    val (message, messageAttributes) = objectMapper.readValue(eventMessage, EventTopicMessage::class.java)
    val eventType = messageAttributes.eventType.Value
    log.info("Received message $message, type $eventType")

    val event = objectMapper.readValue(message, EventMessage::class.java)
    processEvent(event.id)

  }

  private suspend fun processEvent(
      id: UUID
  ) {
    // Go and get data from Event Retrieval API
    val lifeEvent = getEventPayload(id)

    // turn into file
    lifeEvent.details?.let {
      amazonS3.putObject(s3Config.egressBucket, "${lifeEvent.eventId}.csv", it as String)
    }

  }

  suspend fun getEventPayload(id: UUID): EventInformation =
    eventDataRetrievalApiWebClient.get()
      .uri("/events/$id")
      .retrieve()
      .awaitBody()
}

data class AttributeType(val Value: String, val Type: String)
data class MessageAttributes(val eventType: AttributeType, val publisher: AttributeType)
data class EventTopicMessage(
  val Message: String,
  val MessageAttributes: MessageAttributes
)

data class EventMessage(
  val id: UUID,
  val occurredAt: OffsetDateTime,
  val description: String
)
