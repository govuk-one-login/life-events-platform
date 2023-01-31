package uk.gov.gdx.datashare.services

import com.amazonaws.xray.spring.aop.XRayEnabled
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.models.DataProcessorMessage
import uk.gov.gdx.datashare.repositories.AcquirerSubscriptionRepository
import uk.gov.gdx.datashare.repositories.EventData
import uk.gov.gdx.datashare.repositories.EventDataRepository

@Service
@XRayEnabled
class DataProcessor(
  private val objectMapper: ObjectMapper,
  private val acquirerSubscriptionRepository: AcquirerSubscriptionRepository,
  private val eventDataRepository: EventDataRepository,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @JmsListener(destination = "dataprocessor", containerFactory = "awsQueueContainerFactoryProxy")
  fun onGovEvent(message: String) {
    val dataProcessorMessage: DataProcessorMessage = objectMapper.readValue(message, DataProcessorMessage::class.java)
    log.info("Received event [{}] from [{}]", dataProcessorMessage.eventType, dataProcessorMessage.publisher)

    val acquirerSubscriptions = acquirerSubscriptionRepository.findAllByEventType(dataProcessorMessage.eventType)

    val eventData = acquirerSubscriptions.map {
      EventData(
        acquirerSubscriptionId = it.id,
        dataId = dataProcessorMessage.id,
        eventTime = dataProcessorMessage.eventTime,
      )
    }.toList()

    eventDataRepository.saveAll(eventData).toList()
  }
}
