package uk.gov.gdx.datashare.uk.gov.gdx.datashare.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import uk.gov.gdx.datashare.config.AuthenticationFacade
import uk.gov.gdx.datashare.config.DateTimeHandler
import uk.gov.gdx.datashare.config.JacksonConfiguration
import uk.gov.gdx.datashare.config.sha256
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.enums.Sex
import uk.gov.gdx.datashare.models.DeathNotificationDetails
import uk.gov.gdx.datashare.models.EventNotification
import uk.gov.gdx.datashare.repositories.EventApiAudit
import uk.gov.gdx.datashare.repositories.EventApiAuditRepository
import uk.gov.gdx.datashare.services.EventApiAuditService
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class EventApiAuditServiceTest {
  private val eventApiAuditRepository = mockk<EventApiAuditRepository>()
  private val dateTimeHandler = mockk<DateTimeHandler>()
  private val authenticationFacade = mockk<AuthenticationFacade>()
  private val objectMapper = JacksonConfiguration().objectMapper()

  private val underTest = EventApiAuditService(
    eventApiAuditRepository,
    dateTimeHandler,
    authenticationFacade,
    objectMapper,
  )

  @Test
  fun `auditEvents saves no PII`() {
    val request = MockHttpServletRequest("GET", "/events")
    mockkStatic(RequestContextHolder::class)
    every { RequestContextHolder.currentRequestAttributes() }.returns(ServletRequestAttributes(request))

    val oauthClientId = "abcdefg"
    val now = LocalDateTime.now()
    val eventNotification1 = EventNotification(
      eventId = UUID.randomUUID(),
      eventType = EventType.DEATH_NOTIFICATION,
      sourceId = "123456788",
      eventTime = LocalDateTime.now().minusMinutes(2),
      ingestTime = LocalDateTime.now(),
      eventData = DeathNotificationDetails(
        listOf(
          EnrichmentField.FIRST_NAMES,
          EnrichmentField.LAST_NAME,
          EnrichmentField.SEX,
          EnrichmentField.DATE_OF_DEATH,
          EnrichmentField.REGISTRATION_DATE,
        ),
        firstNames = "Alice",
        lastName = "Smith",
        sex = Sex.FEMALE,
        dateOfDeath = LocalDate.of(2023, 1, 2),
        registrationDate = LocalDate.of(2023, 1, 2),
      ),
    )
    val hashedEventData1 = objectMapper.writeValueAsString(eventNotification1.eventData).sha256()
    val eventNotification2 = EventNotification(
      eventId = UUID.randomUUID(),
      eventType = EventType.DEATH_NOTIFICATION,
      sourceId = "123456789",
      eventTime = LocalDateTime.now().minusMinutes(2),
      ingestTime = LocalDateTime.now(),
      eventData = DeathNotificationDetails(
        listOf(
          EnrichmentField.FIRST_NAMES,
          EnrichmentField.LAST_NAME,
          EnrichmentField.SEX,
          EnrichmentField.DATE_OF_DEATH,
          EnrichmentField.REGISTRATION_DATE,
        ),
        firstNames = "Bob",
        lastName = "Smith",
        dateOfDeath = LocalDate.of(2020, 1, 1),
        sex = Sex.MALE,
        registrationDate = LocalDate.of(2023, 1, 2),
      ),
    )
    val hashedEventData2 = objectMapper.writeValueAsString(eventNotification2.eventData).sha256()

    every { authenticationFacade.getUsername() }.returns(oauthClientId)
    every { dateTimeHandler.now() }.returns(now)
    every { eventApiAuditRepository.save(any()) }.returns(
      EventApiAudit(
        oauthClientId = oauthClientId,
        requestMethod = "",
        url = "",
        payload = EventApiAudit.Payload(data = emptyList()),
        whenCreated = now,
      ),
    )

    underTest.auditEventApiCall(listOf(eventNotification1, eventNotification2))

    verify(exactly = 1) {
      eventApiAuditRepository.save(
        withArg {
          assertThat(it.oauthClientId).isEqualTo(oauthClientId)
          assertThat(it.requestMethod).isEqualTo("GET")
          assertThat(it.url).isEqualTo("http://localhost/events")
          assertThat(it.payload.data).contains(
            EventApiAudit.Data(
              eventId = eventNotification1.eventId,
              sourceId = eventNotification1.sourceId,
              hashedEventData = hashedEventData1,
            ),
            EventApiAudit.Data(
              eventId = eventNotification2.eventId,
              sourceId = eventNotification2.sourceId,
              hashedEventData = hashedEventData2,
            ),
          )
          assertThat(it.whenCreated).isEqualTo(now)
        },
      )
    }
    assertThat(hashedEventData1).doesNotContain("Alice")
    assertThat(hashedEventData2).doesNotContain("Bob", "Smith", "2020-01-01")
  }
}
