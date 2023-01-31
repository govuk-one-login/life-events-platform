package uk.gov.gdx.datashare.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.models.EventToPublish
import uk.gov.gdx.datashare.repositories.SupplierSubscriptionRepository
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class PrisonerEventMessageProcessor(
  private val objectMapper: ObjectMapper,
  private val dataReceiverService: DataReceiverService,
  private val supplierSubscriptionRepository: SupplierSubscriptionRepository,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @JmsListener(destination = "prisoner-event", containerFactory = "awsQueueContainerFactoryProxy")
  fun onPrisonerEventMessage(prisonerEventMessage: String) {
    val (message, messageAttributes) = objectMapper.readValue(prisonerEventMessage, TopicMessage::class.java)
    val eventType = messageAttributes.eventType.Value
    log.info("Received message $message, type $eventType")

    val hmppsDomainEvent = objectMapper.readValue(message, HMPPSDomainEvent::class.java)
    when (eventType) {
      "prison-offender-events.prisoner.received" -> processHMPPSEvent(hmppsDomainEvent)
      else -> {
        log.debug("Ignoring message with type $eventType")
      }
    }
  }

  fun processHMPPSEvent(hmppsDomainEvent: HMPPSDomainEvent) {
    // find a supplier with a defined name
    supplierSubscriptionRepository.findAllByEventType(EventType.ENTERED_PRISON)
      .firstOrNull()?.let {
        dataReceiverService.sendToDataProcessor(
          EventToPublish(
            EventType.ENTERED_PRISON,
            LocalDateTime.parse(hmppsDomainEvent.occurredAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME),
            hmppsDomainEvent.additionalInformation.nomsNumber,
          ),
          it.clientId,
        )
      } ?: log.warn("No supplier found for this event type {}", EventType.ENTERED_PRISON)
  }
}

data class TopicEventType(val Value: String, val Type: String)
data class TopicMessageAttributes(val eventType: TopicEventType)
data class TopicMessage(
  val Message: String,
  val MessageAttributes: TopicMessageAttributes,
)

data class HMPPSDomainEvent(
  val eventType: String? = null,
  val additionalInformation: AdditionalInformation,
  val occurredAt: String,
) {
  constructor(
    eventType: String,
    additionalInformation: AdditionalInformation,
    occurredAt: Instant,
  ) : this(
    eventType,
    additionalInformation,
    occurredAt.toOffsetDateFormat(),
  )
}

data class AdditionalInformation(
  val nomsNumber: String,
)
fun Instant.toOffsetDateFormat(): String =
  atZone(ZoneId.of("Europe/London")).toOffsetDateTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
