package uk.gov.gdx.datashare.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import uk.gov.gdx.datashare.config.AuthenticationFacade
import uk.gov.gdx.datashare.config.DateTimeHandler
import uk.gov.gdx.datashare.config.sha256
import uk.gov.gdx.datashare.repository.EventApiAudit
import uk.gov.gdx.datashare.repository.EventApiAuditRepository

@Service
class EventApiAuditService(
  private val eventApiAuditRepository: EventApiAuditRepository,
  private val dateTimeHandler: DateTimeHandler,
  private val authenticationFacade: AuthenticationFacade,
  private val objectMapper: ObjectMapper,
) {
  fun auditEventApiCall(eventNotification: EventNotification) { auditEventApiCall(listOf(eventNotification)) }

  fun auditEventApiCall(eventNotifications: List<EventNotification>) {
    val auditData = EventApiAudit.Payload(
      data = eventNotifications.map {
        EventApiAudit.Data(
          eventId = it.eventId,
          sourceId = it.sourceId,
          hashedEventData = objectMapper.writeValueAsString(it.eventData).sha256(),
        )
      },
    )
    val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request

    val eventApiAudit = EventApiAudit(
      oauthClientId = authenticationFacade.getUsername(),
      requestMethod = request.method,
      url = request.queryString?.let {
        "${request.requestURL}?${request.queryString}"
      } ?: request.requestURL.toString(),
      payload = auditData,
      whenCreated = dateTimeHandler.now(),
    )

    eventApiAuditRepository.save(eventApiAudit)
  }
}
