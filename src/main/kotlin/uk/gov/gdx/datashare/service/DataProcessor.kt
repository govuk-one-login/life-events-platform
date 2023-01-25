package uk.gov.gdx.datashare.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.config.UnknownDatasetException
import uk.gov.gdx.datashare.repository.*
import java.util.*

@Service
class DataProcessor(
  private val objectMapper: ObjectMapper,
  private val consumerSubscriptionRepository: ConsumerSubscriptionRepository,
  private val eventDataRepository: EventDataRepository,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @JmsListener(destination = "dataprocessor", containerFactory = "awsQueueContainerFactoryProxy")
  fun onGovEvent(message: String) {
    val dataProcessorMessage: DataProcessorMessage = objectMapper.readValue(message, DataProcessorMessage::class.java)
    log.info("Received event [{}] from [{}]", dataProcessorMessage.eventTypeId, dataProcessorMessage.publisher)

    val dataId = dataProcessorMessage.id ?: UUID.randomUUID().toString()

    if (dataProcessorMessage.datasetId != "DEATH_LEV" && dataProcessorMessage.datasetId != "PASS_THROUGH") {
      throw UnknownDatasetException("Unknown DataSet ${dataProcessorMessage.datasetId}")
    }

    val consumerSubscriptions = consumerSubscriptionRepository.findAllByEventType(dataProcessorMessage.eventTypeId)

    val eventData = consumerSubscriptions.map {
      EventData(
        consumerSubscriptionId = it.id,
        datasetId = dataProcessorMessage.datasetId,
        dataId = dataId,
        eventTime = dataProcessorMessage.eventTime,
      )
    }.toList()

    eventDataRepository.saveAll(eventData).toList()
  }
}
