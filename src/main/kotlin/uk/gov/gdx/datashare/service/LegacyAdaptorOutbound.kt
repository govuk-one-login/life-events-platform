package uk.gov.gdx.datashare.service

import com.amazonaws.services.s3.AmazonS3
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import net.javacrumbs.shedlock.core.LockAssert
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.JmsListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody
import uk.gov.gdx.datashare.config.S3Config
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.TimeUnit

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

  @Scheduled(fixedRate = 30, timeUnit = TimeUnit.MINUTES)
  @SchedulerLock(name = "publishToS3Bucket", lockAtMostFor = "50s", lockAtLeastFor = "50s")
  fun publishToS3Bucket() {
    try {
      runBlocking {
        LockAssert.assertLocked()
      }
    } catch (e: Exception) {
      log.error("Exception", e)
    }
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
    log.debug("Obtained event: ${lifeEvent.eventId}")

    // turn into file
    lifeEvent.eventData?.let {
      log.debug("Writing to S3 bucket")
      amazonS3.putObject(s3Config.egressBucket, "${lifeEvent.eventId}.json", objectMapper.writeValueAsString(it))
      removeEvent(lifeEvent.eventId)
    }

  }

  suspend fun getEventPayload(id: UUID): EventNotification =
    eventDataRetrievalApiWebClient.get()
      .uri("/events/$id")
      .retrieve()
      .awaitBody()

  suspend fun removeEvent(id: UUID) =
    eventDataRetrievalApiWebClient.delete()
      .uri("/events/$id")
      .retrieve()
      .awaitBodilessEntity()
}

data class AttributeType(val Value: String, val Type: String)
data class MessageAttributes(val eventType: AttributeType, val consumer: AttributeType)
data class EventTopicMessage(
  val Message: String,
  val MessageAttributes: MessageAttributes
)

data class EventMessage(
  val id: UUID,
  val occurredAt: OffsetDateTime,
  val description: String
)
