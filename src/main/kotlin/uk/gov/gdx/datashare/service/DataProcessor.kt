package uk.gov.gdx.datashare.service

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.repository.*
import java.util.*

@Service
class DataProcessor(
  private val objectMapper: ObjectMapper,
  private val deathNotificationService: DeathNotificationService,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @JmsListener(destination = "dataprocessor", containerFactory = "awsQueueContainerFactoryProxy")
  fun onGovEvent(message: String) {
    val dataProcessorMessage: DataProcessorMessage = objectMapper.readValue(message, DataProcessorMessage::class.java)
    log.info("Received event [{}] from [{}]", dataProcessorMessage.eventTypeId, dataProcessorMessage.publisher)

    // lookup provider
    val details = getDataFromProvider(dataProcessorMessage)

    when (dataProcessorMessage.eventTypeId) {
      "DEATH_NOTIFICATION" -> deathNotificationService.saveDeathNotificationEvents(
        details,
        dataProcessorMessage,
      )

      "LIFE_EVENT" -> print("x == 2")
    }
  }
}

private fun getDataFromProvider(dataProcessorMessage: DataProcessorMessage): DataDetail {
  val id = dataProcessorMessage.id ?: UUID.randomUUID().toString()
  val dataPayload = if (dataProcessorMessage.storePayload) dataProcessorMessage.details else null

  return when (dataProcessorMessage.datasetId) {
    "DEATH_LEV" -> {
      DataDetail(id = id)
    }

    else -> {
      DataDetail(id = id, data = dataPayload)
    }
  }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DataDetail(
  var id: String,
  var data: Any? = null,
)
