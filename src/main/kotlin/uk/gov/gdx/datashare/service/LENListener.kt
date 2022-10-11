package uk.gov.gdx.datashare.service

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service

@Service
class LENListener(
  private val gdxEventIngesterService: GdxEventIngesterService,
  private val mapper: ObjectMapper
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @JmsListener(destination = "len", containerFactory = "hmppsQueueContainerFactoryProxy")
  fun onLENEvent(requestJson: String) = runBlocking {
    val (message, messageAttributes) = mapper.readValue(requestJson, LENMessage::class.java)
    val eventType = messageAttributes.eventType.Value
    log.info("Received message $message, type $eventType")

    when (eventType) {
      "death" -> {
        val lenEvent = mapper.readValue(message, LENEvent::class.java)
        gdxEventIngesterService.storeAndPublishLenEvent(lenEvent)
      }
      else -> {
        log.debug("Ignoring message with type $eventType")
      }
    }
  }
}

data class LENEventType(val Value: String, val Type: String)
data class LENMessageAttributes(val eventType: LENEventType)
data class LENMessage(
  val Message: String,
  val MessageAttributes: LENMessageAttributes
)

data class LENEvent(
  val eventType: String,
  val id: Long
)
