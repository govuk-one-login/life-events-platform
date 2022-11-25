package uk.gov.gdx.datashare.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.repository.EventData
import uk.gov.gdx.datashare.repository.EventDataRepository
import uk.gov.gdx.datashare.resource.EventType
import java.time.LocalDateTime
import java.util.UUID

@Service
class EventPublishingService(
  private val dataShareTopicService: DataShareTopicService,
  private val auditService: AuditService,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun storeAndPublishEvent(event: EventData) {
    // publish the change
    log.debug("Publishing new event {} {} from {}", event.eventId, event.eventType, event.dataProvider)

    dataShareTopicService.sendGovEvent(
      eventId = event.eventId,
      eventType = EventType.valueOf(event.eventType),
      occurredAt = event.whenCreated?: LocalDateTime.now()
    )

    // audit the event
    auditService.sendMessage(
      auditType = AuditType.DATA_SHARE_EVENT_PUBLISHED,
      id = event.eventId,
      details = event.eventType,
      username = event.dataProvider)
  }
}
