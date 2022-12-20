package uk.gov.gdx.datashare.service

import com.amazonaws.services.sns.model.MessageAttributeValue
import com.amazonaws.services.sns.model.PublishRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class DataShareTopicService(hmppsQueueService: HmppsQueueService, private val objectMapper: ObjectMapper) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  private val domaineventsTopic by lazy { hmppsQueueService.findByTopicId("event") ?: throw RuntimeException("Topic with name event doesn't exist") }
  private val domaineventsTopicClient by lazy { domaineventsTopic.snsClient }

  fun sendGovEvent(eventId: String, occurredAt: LocalDateTime, eventType: String) {
    publishToDomainEventsTopic(
      DataShareEvent(
        eventType,
        eventId,
        occurredAt.atZone(ZoneId.systemDefault()).toInstant(),
        "Gov Event: $eventType"
      )
    )
  }

  private fun publishToDomainEventsTopic(payload: DataShareEvent) {
    log.debug("Event {} for id {}", payload.eventType, payload.id)
    domaineventsTopicClient.publish(
      PublishRequest(domaineventsTopic.arn, objectMapper.writeValueAsString(payload))
        .withMessageAttributes(
          mapOf(
            "eventType" to MessageAttributeValue().withDataType("String").withStringValue(payload.eventType)
          )
        )
        .also { log.info("Published event $payload to outbound topic") }
    )
  }
}

data class DataShareEvent(
  val eventType: String? = null,
  val id: String,
  val version: String,
  val occurredAt: String,
  val description: String
) {
  constructor(
    eventType: String,
    id: String,
    occurredAt: Instant,
    description: String,
  ) : this(
    eventType,
    id,
    "1.0",
    occurredAt.toOffsetDateFormat(),
    description
  )
}
fun Instant.toOffsetDateFormat(): String =
  atZone(ZoneId.of("Europe/London")).toOffsetDateTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
