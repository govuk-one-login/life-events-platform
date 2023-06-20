package uk.gov.gdx.datashare.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import uk.gov.gdx.datashare.config.AuthenticationFacade
import uk.gov.gdx.datashare.config.DateTimeHandler
import uk.gov.gdx.datashare.config.sha256
import uk.gov.gdx.datashare.models.EventNotification
import uk.gov.gdx.datashare.repositories.AcquirerEventAudit
import uk.gov.gdx.datashare.repositories.AcquirerEventAuditRepository

@Service
class AcquirerEventAuditService(
  private val acquirerEventAuditRepository: AcquirerEventAuditRepository,
  private val dateTimeHandler: DateTimeHandler,
  private val authenticationFacade: AuthenticationFacade,
  private val objectMapper: ObjectMapper,
) {
  fun auditEventApiCall(eventNotification: EventNotification) { auditEventApiCall(listOf(eventNotification)) }

  fun auditEventApiCall(eventNotifications: List<EventNotification>) {
    val auditData = auditDataFromEventNotifications(eventNotifications)
    val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request

    val acquirerEventAudit = AcquirerEventAudit(
      oauthClientId = authenticationFacade.getUsername(),
      requestMethod = request.method,
      url = request.queryString?.let {
        "${request.requestURL}?${request.queryString}"
      } ?: request.requestURL.toString(),
      payload = auditData,
      createdAt = dateTimeHandler.now(),
      queueName = null,
    )

    acquirerEventAuditRepository.save(acquirerEventAudit)
  }

  fun auditQueuedEventMessage(eventNotification: EventNotification, queueName: String) {
    val auditData = auditDataFromEventNotifications(listOf(eventNotification))
    val acquirerEventAudit = AcquirerEventAudit(
      oauthClientId = null,
      requestMethod = null,
      url = null,
      payload = auditData,
      createdAt = dateTimeHandler.now(),
      queueName = queueName,
    )

    acquirerEventAuditRepository.save(acquirerEventAudit)
  }

  private fun auditDataFromEventNotifications(eventNotifications: List<EventNotification>) =
    AcquirerEventAudit.Payload(
      data = eventNotifications.map {
        AcquirerEventAudit.Data(
          eventId = it.eventId,
          sourceId = it.sourceId,
          hashedEventData = objectMapper.writeValueAsString(it.eventData).sha256(),
        )
      },
    )
}
