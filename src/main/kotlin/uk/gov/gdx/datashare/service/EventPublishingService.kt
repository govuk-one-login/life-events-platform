package uk.gov.gdx.datashare.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class EventPublishingService(
  private val dataShareTopicService: DataShareTopicService,
  private val auditService: AuditService
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun storeAndPublishEvent(eventId: String, dataProcessorMessage: DataProcessorMessage) {
    // publish the change
    log.debug("Publishing new event {} {} {} from {}", eventId, dataProcessorMessage.eventTypeId, dataProcessorMessage.datasetId, dataProcessorMessage.publisher)

    dataShareTopicService.sendGovEvent(
      eventId = eventId,
      eventType = dataProcessorMessage.eventTypeId,
      occurredAt = dataProcessorMessage.eventTime
    )

    // audit the event
    auditService.sendMessage(
      auditType = AuditType.DATA_SHARE_EVENT_PUBLISHED,
      id = eventId,
      details = dataProcessorMessage.eventTypeId,
      username = dataProcessorMessage.publisher
    )
  }
}
