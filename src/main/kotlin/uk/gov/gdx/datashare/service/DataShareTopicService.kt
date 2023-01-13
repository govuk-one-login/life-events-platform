package uk.gov.gdx.datashare.service

import com.amazonaws.services.sns.model.MessageAttributeValue
import com.amazonaws.services.sns.model.PublishRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.queue.AwsQueueService
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class DataShareTopicService(awsQueueService: AwsQueueService, private val objectMapper: ObjectMapper) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  private val domainEventsTopic by lazy {
    awsQueueService.findByTopicId("event") ?: throw RuntimeException("Topic with name event doesn't exist")
  }
  private val domainEventsTopicClient by lazy { domainEventsTopic.snsClient }

  fun sendGovEvent(eventId: UUID, consumer: String, occurredAt: LocalDateTime, eventType: String) {
    publishToDomainEventsTopic(
      DataShareEvent(
        eventType,
        eventId,
        consumer,
        occurredAt.atZone(ZoneId.systemDefault()).toInstant(),
        "Gov Event: $eventType",
      ),
    )
  }

  private fun publishToDomainEventsTopic(payload: DataShareEvent) {
    log.debug("Event {} for id {}", payload.eventType, payload.id)
    domainEventsTopicClient.publish(
      PublishRequest(domainEventsTopic.arn, objectMapper.writeValueAsString(payload))
        .withMessageAttributes(
          mapOf(
            "eventType" to MessageAttributeValue().withDataType("String").withStringValue(payload.eventType),
            "consumer" to MessageAttributeValue().withDataType("String").withStringValue(payload.consumer.toString()),
          ),
        )
        .also { log.info("Published event $payload to outbound topic") },
    )
  }
}

data class DataShareEvent(
  val eventType: String? = null,
  val id: UUID,
  val consumer: String,
  val version: String,
  val occurredAt: String,
  val description: String,
) {
  constructor(
    eventType: String,
    id: UUID,
    consumer: String,
    occurredAt: Instant,
    description: String,
  ) : this(
    eventType,
    id,
    consumer,
    "1.0",
    occurredAt.toOffsetDateFormat(),
    description,
  )
}

fun Instant.toOffsetDateFormat(): String =
  atZone(ZoneId.of("Europe/London")).toOffsetDateTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
