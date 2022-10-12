package uk.gov.gdx.datashare.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.repository.EventLookup
import uk.gov.gdx.datashare.repository.EventLookupRepository
import java.time.LocalDateTime
import java.util.UUID

@Service
class GdxEventIngesterService(
  private val dataShareTopicService: DataShareTopicService,
  private val eventLookupRepository: EventLookupRepository,
  private val auditService: AuditService,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun storeAndPublishLenEvent(lenEvent: LENEvent) {
    log.info("Received LEN event [{}] [{}]", lenEvent.eventType, lenEvent.id)

    // audit the event
    auditService.sendMessage(auditType = AuditType.LEN_EVENT_OCCURRED, id = lenEvent.id.toString(), details = lenEvent, username = "LEN")

    // Store the ID in the lookup table
    val eventId = UUID.randomUUID().toString()
    val eventLookup = EventLookup(eventId = eventId, levLookupId = lenEvent.id, eventType = lenEvent.eventType)
    eventLookupRepository.save(eventLookup)

    // publish the change
    dataShareTopicService.sendCitizenEvent(eventId = eventId, eventType = DataShareEventType.CITIZEN_DEATH, occurredAt = LocalDateTime.now())

    // audit the event
    auditService.sendMessage(auditType = AuditType.DATA_SHARE_EVENT_PUBLISHED, id = eventId, details = eventLookup, username = "GDX")
  }
}
