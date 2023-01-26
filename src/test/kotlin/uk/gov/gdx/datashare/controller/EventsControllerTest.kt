package uk.gov.gdx.datashare.controller

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.*
import org.approvaltests.Approvals
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.gdx.datashare.config.EventNotFoundException
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.enums.Sex
import uk.gov.gdx.datashare.models.DeathNotificationDetails
import uk.gov.gdx.datashare.models.EventNotification
import uk.gov.gdx.datashare.models.EventStatus
import uk.gov.gdx.datashare.models.EventToPublish
import uk.gov.gdx.datashare.models.Events
import uk.gov.gdx.datashare.service.*
import uk.gov.gdx.datashare.service.DataReceiverService
import uk.gov.gdx.datashare.service.EventDataService
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Stream

class EventsControllerTest {
  private val eventDataService = mockk<EventDataService>()
  private val dataReceiverService = mockk<DataReceiverService>()
  private val eventApiAuditService = mockk<EventApiAuditService>()
  private val meterRegistry = mockk<MeterRegistry>()
  private val publishEventCounter = mockk<Counter>()
  private val getEventCounter = mockk<Counter>()
  private val getEventsCounter = mockk<Counter>()
  private val getEventsStatusCounter = mockk<Counter>()
  private val deleteEventCounter = mockk<Counter>()
  private val deleteEventSuccessCounter = mockk<Counter>()
  private val publishEventSuccessCounter = mockk<Counter>()

  private val underTest: EventsController

  init {
    every { meterRegistry.counter("API_CALLS.PublishEvent", *anyVararg()) }.returns(publishEventCounter)
    every { meterRegistry.counter("API_CALLS.GetEvent", *anyVararg()) }.returns(getEventCounter)
    every { meterRegistry.counter("API_CALLS.GetEvents", *anyVararg()) }.returns(getEventsCounter)
    every { meterRegistry.counter("API_CALLS.GetEventsStatus", *anyVararg()) }.returns(getEventsStatusCounter)
    every { meterRegistry.counter("API_CALLS.DeleteEvent", *anyVararg()) }.returns(deleteEventCounter)
    every { meterRegistry.counter("SUCCESSFUL_API_CALLS.PublishEvent", *anyVararg()) }.returns(
      publishEventSuccessCounter,
    )
    every { meterRegistry.counter("SUCCESSFUL_API_CALLS.DeleteEvent", *anyVararg()) }.returns(deleteEventSuccessCounter)
    every { publishEventCounter.increment() }.returns(Unit)
    every { getEventCounter.increment() }.returns(Unit)
    every { getEventsCounter.increment() }.returns(Unit)
    every { getEventsStatusCounter.increment() }.returns(Unit)
    every { deleteEventCounter.increment() }.returns(Unit)
    every { deleteEventSuccessCounter.increment() }.returns(Unit)
    every { publishEventSuccessCounter.increment() }.returns(Unit)
    underTest = EventsController(eventDataService, dataReceiverService, eventApiAuditService, meterRegistry)
  }

  @ParameterizedTest
  @MethodSource("provideLocalDateTimes")
  fun `getEventsStatus gets events status`(startTime: LocalDateTime?, endTime: LocalDateTime?) {
    val eventStatuses = listOf(
      EventStatus(
        eventType = EventType.DEATH_NOTIFICATION,
        count = 123,
      ),
      EventStatus(
        eventType = EventType.LIFE_EVENT,
        count = 456,
      ),
    )

    every { eventDataService.getEventsStatus(startTime, endTime) }.returns(eventStatuses)

    val eventStatusesOutput = underTest.getEventsStatus(startTime, endTime)

    assertThat(eventStatusesOutput).hasSize(2)
    assertThat(eventStatusesOutput).isEqualTo(eventStatuses.toList())
    verify(exactly = 1) { getEventsStatusCounter.increment() }
  }

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
        enrichmentFields = "firstNames",
        eventData = DeathNotificationDetails(
          firstNames = "Bob",
        ),
      ),
      EventNotification(
        eventId = UUID.randomUUID(),
        eventType = EventType.DEATH_NOTIFICATION,
        sourceId = UUID.randomUUID().toString(),
        dataIncluded = true,
        enrichmentFields = "firstNames,lastName",
        eventData = DeathNotificationDetails(
          firstNames = "Bob",
          lastName = "Smith",
        ),
      ),
    )

    every { eventDataService.getEvents(eventTypes, startTime, endTime, 0, 10) }.returns(Events(events.count(), events))
    every { eventApiAuditService.auditEventApiCall(events) }.returns(Unit)

    val eventsOutput = underTest.getEvents(eventTypes, startTime, endTime, 0, 10)

    assertThat(eventsOutput.content).hasSize(2)
    assertThat(eventsOutput.content.map { it.content }).isEqualTo(events)
    assertThat(eventsOutput.metadata?.totalElements).isEqualTo(2)
    verify(exactly = 1) { getEventsCounter.increment() }
    verify(exactly = 1) { eventApiAuditService.auditEventApiCall(events) }
  }

  @Test
  fun `publishEvent sends event to processor`() {
    val event = EventToPublish(
      eventType = EventType.DEATH_NOTIFICATION,
      eventTime = LocalDateTime.now(),
      id = "123456789",
    )

    every { dataReceiverService.sendToDataProcessor(any()) }.returns(Unit)

    underTest.publishEvent(event)

    verify(exactly = 1) { publishEventCounter.increment() }
    verify(exactly = 1) { dataReceiverService.sendToDataProcessor(event) }
  }

  @Test
  fun `getEvent gets event`() {
    val event = EventNotification(
      eventId = UUID.randomUUID(),
      eventType = EventType.DEATH_NOTIFICATION,
      sourceId = UUID.randomUUID().toString(),
      eventData = DeathNotificationDetails(
        firstNames = "Bob",
        registrationDate = LocalDate.of(2023, 1, 3),
        dateOfBirth = LocalDate.of(1954, 1, 3),
        dateOfDeath = LocalDate.of(2023, 1, 2),
        sex = Sex.INDETERMINATE,
      ),
    )

    every { eventDataService.getEvent(event.eventId) }.returns(event)
    every { eventApiAuditService.auditEventApiCall(event) }.returns(Unit)

    val eventOutput = underTest.getEvent(event.eventId)

    assertThat(eventOutput?.content).isEqualTo(event)
    verify(exactly = 1) { getEventCounter.increment() }
    verify(exactly = 1) { eventApiAuditService.auditEventApiCall(event) }
  }

  @Test
  fun `deleteEvent deletes event`() {
    val event = EventNotification(
      eventId = UUID.randomUUID(),
      eventType = EventType.DEATH_NOTIFICATION,
      sourceId = UUID.randomUUID().toString(),
      eventData = null,
    )

    every { eventDataService.deleteEvent(event.eventId) }.returns(event)
    every { eventApiAuditService.auditEventApiCall(event) }.returns(Unit)

    underTest.deleteEvent(event.eventId)

    verify(exactly = 1) { eventDataService.deleteEvent(event.eventId) }
    verify(exactly = 1) { deleteEventCounter.increment() }
    verify(exactly = 1) { eventApiAuditService.auditEventApiCall(event) }
  }

  @Test
  fun `getEvent returns exception when no event found`() {
    val eventId = UUID.randomUUID()

    try {
      every { eventDataService.getEvent(any()) }.throws(EventNotFoundException("Event Not Found"))
    } catch (e: EventNotFoundException) {
      underTest.getEvent(eventId)

      verify(exactly = 1) { eventDataService.getEvent(eventId) }
    }
  }

  @Test
  fun `getEvents gets events of expected shape`() {
    val eventTypes = listOf(EventType.DEATH_NOTIFICATION)
    val events = listOf(
      EventNotification(
        eventId = UUID.fromString("a5383689-1192-4078-a4a6-a611b0a34c6e"),
        eventType = EventType.DEATH_NOTIFICATION,
        sourceId = "a5383689-1192-4078-a4a6-a611b0a34c6e",
        eventData = DeathNotificationDetails(
          firstNames = "Bob",
        ),
      ),
      EventNotification(
        eventId = UUID.fromString("ec39aa80-2fa2-4d46-9211-c66fc94024d3"),
        eventType = EventType.DEATH_NOTIFICATION,
        sourceId = "ec39aa80-2fa2-4d46-9211-c66fc94024d3",
        eventData = DeathNotificationDetails(
          firstNames = "Bob",
          lastName = "Smith",
        ),
      ),
    )

    every { eventDataService.getEvents(eventTypes, any(), any(), any(), any()) }.returns(Events(events.count(), events))
    every { eventApiAuditService.auditEventApiCall(events) }.returns(Unit)

    val eventsOutput = underTest.getEvents(eventTypes, pageNumber = 0, pageSize = 10)

    Approvals.verify(eventsOutput)
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
