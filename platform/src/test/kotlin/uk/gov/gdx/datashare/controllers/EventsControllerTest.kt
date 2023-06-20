package uk.gov.gdx.datashare.controllers

import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.gdx.datashare.config.EventNotFoundException
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.enums.Sex
import uk.gov.gdx.datashare.models.DeathNotificationDetails
import uk.gov.gdx.datashare.models.EventNotification
import uk.gov.gdx.datashare.models.EventToPublish
import uk.gov.gdx.datashare.models.Events
import uk.gov.gdx.datashare.repositories.SupplierEvent
import uk.gov.gdx.datashare.services.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Stream

class EventsControllerTest {
  private val acquirerEventService = mockk<AcquirerEventService>()
  private val acquirerEventAuditService = mockk<AcquirerEventAuditService>()
  private val eventAcceptorService = mockk<EventAcceptorService>()

  private val underTest = EventsController(acquirerEventService, acquirerEventAuditService, eventAcceptorService)

  @ParameterizedTest
  @MethodSource("provideLocalDateTimes")
  fun `getEvents gets events`(startTime: LocalDateTime?, endTime: LocalDateTime?) {
    val eventTypes = listOf(EventType.DEATH_NOTIFICATION)
    val events = listOf(
      EventNotification(
        eventId = UUID.randomUUID(),
        eventType = EventType.DEATH_NOTIFICATION,
        sourceId = UUID.randomUUID().toString(),
        dataIncluded = true,
        enrichmentFields = listOf(EnrichmentField.FIRST_NAMES),
        eventData = DeathNotificationDetails(
          listOf(EnrichmentField.FIRST_NAMES),
          firstNames = "Bob",
        ),
      ),
      EventNotification(
        eventId = UUID.randomUUID(),
        eventType = EventType.DEATH_NOTIFICATION,
        sourceId = UUID.randomUUID().toString(),
        dataIncluded = true,
        enrichmentFields = listOf(EnrichmentField.FIRST_NAMES, EnrichmentField.LAST_NAME),
        eventData = DeathNotificationDetails(
          listOf(EnrichmentField.FIRST_NAMES, EnrichmentField.LAST_NAME),
          firstNames = "Bob",
          lastName = "Smith",
        ),
      ),
    )

    every { acquirerEventService.getEvents(eventTypes, startTime, endTime, 0, 10) }.returns(Events(events.count(), events))
    every { acquirerEventAuditService.auditEventApiCall(events) }.returns(Unit)

    val eventsOutput = underTest.getEvents(eventTypes, startTime, endTime, 0, 10)

    assertThat(eventsOutput.content).hasSize(2)
    assertThat(eventsOutput.content.map { it.content }).isEqualTo(events)
    assertThat(eventsOutput.metadata?.totalElements).isEqualTo(2)
    verify(exactly = 1) { acquirerEventAuditService.auditEventApiCall(events) }
  }

  @Test
  fun `getEvent gets event`() {
    val event = EventNotification(
      eventId = UUID.randomUUID(),
      eventType = EventType.DEATH_NOTIFICATION,
      sourceId = UUID.randomUUID().toString(),
      eventData = DeathNotificationDetails(
        listOf(
          EnrichmentField.FIRST_NAMES,
          EnrichmentField.DATE_OF_BIRTH,
          EnrichmentField.DATE_OF_DEATH,
          EnrichmentField.REGISTRATION_DATE,
          EnrichmentField.SEX,
        ),
        firstNames = "Bob",
        registrationDate = LocalDate.of(2023, 1, 3),
        dateOfBirth = LocalDate.of(1954, 1, 3),
        dateOfDeath = LocalDate.of(2023, 1, 2),
        sex = Sex.INDETERMINATE,
      ),
    )

    every { acquirerEventService.getEvent(event.eventId) }.returns(event)
    every { acquirerEventAuditService.auditEventApiCall(event) }.returns(Unit)

    val eventOutput = underTest.getEvent(event.eventId)

    assertThat(eventOutput?.content).isEqualTo(event)
    verify(exactly = 1) { acquirerEventAuditService.auditEventApiCall(event) }
  }

  @Test
  fun `deleteEvent deletes event`() {
    val event = EventNotification(
      eventId = UUID.randomUUID(),
      eventType = EventType.DEATH_NOTIFICATION,
      sourceId = UUID.randomUUID().toString(),
      eventData = null,
    )

    every { acquirerEventService.deleteEvent(event.eventId) }.returns(event)
    every { acquirerEventAuditService.auditEventApiCall(event) }.returns(Unit)

    underTest.deleteEvent(event.eventId)

    verify(exactly = 1) { acquirerEventService.deleteEvent(event.eventId) }
    verify(exactly = 1) { acquirerEventAuditService.auditEventApiCall(event) }
  }

  @Test
  fun `getEvent returns exception when no event found`() {
    val eventId = UUID.randomUUID()

    try {
      every { acquirerEventService.getEvent(any()) }.throws(EventNotFoundException("Event Not Found"))
    } catch (e: EventNotFoundException) {
      underTest.getEvent(eventId)

      verify(exactly = 1) { acquirerEventService.getEvent(eventId) }
    }
  }

  @Test
  fun `publishEvent sends event to processor`() {
    val event = EventToPublish(
      eventType = EventType.DEATH_NOTIFICATION,
      eventTime = LocalDateTime.now(),
      id = "123456789",
    )
    val supplierEvent = mockk<SupplierEvent>()

    every { eventAcceptorService.acceptEvent(any()) }.returns(supplierEvent)

    underTest.publishEvent(event)

    verify(exactly = 1) { eventAcceptorService.acceptEvent(event) }
  }

  companion object {
    @JvmStatic
    private fun provideLocalDateTimes(): Stream<Arguments?>? {
      return Stream.of(
        Arguments.of(LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1)),
        Arguments.of(null, null),
      )
    }
  }
}
