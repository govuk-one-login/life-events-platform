package uk.gov.gdx.datashare.service

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.repository.*
import java.util.*

@Service
class DataProcessor(
  private val auditService: AuditService,
  private val ingressEventDataRepository: IngressEventDataRepository,
  private val objectMapper: ObjectMapper,
  private val deathNotificationService: DeathNotificationService
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @JmsListener(destination = "dataprocessor", containerFactory = "hmppsQueueContainerFactoryProxy")
  fun onGovEvent(message: String) {

    runBlocking {
      val dataProcessorMessage: DataProcessorMessage = objectMapper.readValue(message, DataProcessorMessage::class.java)
      log.info("Received event [{}] from [{}]", dataProcessorMessage.eventTypeId, dataProcessorMessage.publisher)

      // audit the event
      auditService.sendMessage(
        auditType = AuditType.EVENT_OCCURRED,
        id = dataProcessorMessage.eventTypeId,
        details = dataProcessorMessage.details ?: "NONE",
        username = dataProcessorMessage.publisher
      )

      val ingressEventId = UUID.randomUUID()

      // lookup provider
      val details = getDataFromProvider(ingressEventId, dataProcessorMessage)

      val eventData = IngressEventData(
        eventId = ingressEventId,
        eventTypeId = dataProcessorMessage.eventTypeId,
        subscriptionId = dataProcessorMessage.subscriptionId,
        datasetId = dataProcessorMessage.datasetId,
        dataId = details.id,
        dataPayload = details.data as String?,
        whenCreated = dataProcessorMessage.eventTime,
        dataExpiryTime = dataProcessorMessage.eventTime.plusHours(1)
      )

      ingressEventDataRepository.save(eventData)

      when (eventData.eventTypeId) {
        "DEATH_NOTIFICATION" -> deathNotificationService.saveDeathNotificationEvents(eventData, details, dataProcessorMessage)
        "LIFE_EVENT" -> print("x == 2")
      }
    }
  }

  private fun getDataFromProvider(eventId: UUID, dataProcessorMessage: DataProcessorMessage): DataDetail {
    val id = dataProcessorMessage.id ?: eventId.toString()
    val dataPayload = if (dataProcessorMessage.storePayload) dataProcessorMessage.details else null

    return when (dataProcessorMessage.datasetId) {
      "DEATH_CSV" -> {
        DataDetail(id = id, data = dataPayload)
      }
      "DEATH_LEV" -> {
        DataDetail(id = id)
      }
      else -> {
        DataDetail(id = id, data = dataPayload)
      }
    }
  }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DataDetail(
  var id: String,
  var data: Any? = null
)
