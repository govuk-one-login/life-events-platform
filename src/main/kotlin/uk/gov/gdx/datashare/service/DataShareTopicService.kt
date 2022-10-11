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

  private val domaineventsTopic by lazy { hmppsQueueService.findByTopicId("datashare") ?: throw RuntimeException("Topic with name datashare doesn't exist") }
  private val domaineventsTopicClient by lazy { domaineventsTopic.snsClient }

  fun sendCitizenEvent(eventId: String, occurredAt: LocalDateTime, eventType: DataShareEventType) {
    publishToDomainEventsTopic(
      DataShareEvent(
        eventType.value,
        eventId,
        occurredAt.atZone(ZoneId.systemDefault()).toInstant(),
        "Citizen Event: ${eventType.value}"
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
    description: String
  ) : this(
    eventType,
    id,
    "1.0",
    occurredAt.toOffsetDateFormat(),
    description
  )
}

enum class DataShareEventType(val value: String) {
  CITIZEN_DEATH("citizen-death"),
}

fun Instant.toOffsetDateFormat(): String =
  atZone(ZoneId.of("Europe/London")).toOffsetDateTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
