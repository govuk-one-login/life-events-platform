package uk.gov.gdx.datashare.controller

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.gdx.datashare.service.DataReceiverService
import uk.gov.gdx.datashare.service.DeathNotificationDetails
import uk.gov.gdx.datashare.service.EventDataService
import uk.gov.gdx.datashare.service.EventNotification
import uk.gov.gdx.datashare.service.EventStatus
import java.time.LocalDateTime
import java.util.UUID
import java.util.stream.Stream

class EventsControllerTest {
  private val eventDataService = mockk<EventDataService>()
  private val dataReceiverService = mockk<DataReceiverService>()
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
    underTest = EventsController(eventDataService, dataReceiverService, meterRegistry)
  }

  @ParameterizedTest
  @MethodSource("provideLocalDateTimes")
  fun `getEventsStatus gets events status`(startTime: LocalDateTime?, endTime: LocalDateTime?) {
    runBlocking {
      val eventStatuses = flowOf(
        EventStatus(
          eventType = "DEATH_NOTIFICATION",
          count = 123,
        ),
        EventStatus(
          eventType = "LIFE_EVENT",
          count = 456,
        ),
      )

      coEvery { eventDataService.getEventsStatus(startTime, endTime) }.returns(eventStatuses)

      val eventStatusesOutput = underTest.getEventsStatus(startTime, endTime)

      assertThat(eventStatusesOutput).hasSize(2)
      assertThat(eventStatusesOutput).isEqualTo(eventStatuses.toList())
      verify(exactly = 1) { getEventsStatusCounter.increment() }
    }
  }

  @ParameterizedTest
  @MethodSource("provideLocalDateTimes")
  fun `getEvents gets events`(startTime: LocalDateTime?, endTime: LocalDateTime?) {
    runBlocking {
      val eventTypes = listOf("DEATH_NOTIFICATION")
      val events = flowOf(
        EventNotification(
          eventId = UUID.randomUUID(),
          eventType = "DEATH_NOTIFICATION",
          sourceId = UUID.randomUUID().toString(),
          eventData = DeathNotificationDetails(
            firstName = "Bob",
          ),
        ),
        EventNotification(
          eventId = UUID.randomUUID(),
          eventType = "DEATH_NOTIFICATION",
          sourceId = UUID.randomUUID().toString(),
          eventData = DeathNotificationDetails(
            firstName = "Bob",
            lastName = "Smith",
          ),
        ),
      )

      coEvery { eventDataService.getEvents(eventTypes, startTime, endTime) }.returns(events)

      val eventsOutput = underTest.getEvents(eventTypes, startTime, endTime)

      assertThat(eventsOutput).hasSize(2)
      assertThat(eventsOutput).isEqualTo(events.toList())
      verify(exactly = 1) { getEventsCounter.increment() }
    }
  }

  @Test
  fun `publishEvent sends event to processor`() {
    runBlocking {
      val event = EventToPublish(
        eventType = "DEATH_NOTIFICATION",
        eventTime = LocalDateTime.now(),
        id = "123456789",
        eventDetails = "{\"firstName\":\"Bob\"}",
      )

      coEvery { dataReceiverService.sendToDataProcessor(any()) }.returns(Unit)

      underTest.publishEvent(event)

      verify(exactly = 1) { publishEventCounter.increment() }
      coVerify(exactly = 1) { dataReceiverService.sendToDataProcessor(event) }
    }
  }

  @Test
  fun `getEvent gets event`() {
    runBlocking {
      val event = EventNotification(
        eventId = UUID.randomUUID(),
        eventType = "DEATH_NOTIFICATION",
        sourceId = UUID.randomUUID().toString(),
        eventData = DeathNotificationDetails(
          firstName = "Bob",
        ),
      )

      coEvery { eventDataService.getEvent(event.eventId) }.returns(event)

      val eventOutput = underTest.getEvent(event.eventId)

      assertThat(eventOutput).isEqualTo(event)
      verify(exactly = 1) { getEventCounter.increment() }
    }
  }

  @Test
  fun `deleteEvent deletes event`() {
    runBlocking {
      val eventId = UUID.randomUUID()

      coEvery { eventDataService.deleteEvent(any()) }.returns(Unit)

      underTest.deleteEvent(eventId)

      coVerify(exactly = 1) { eventDataService.deleteEvent(eventId) }
      verify(exactly = 1) { deleteEventCounter.increment() }
    }
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
