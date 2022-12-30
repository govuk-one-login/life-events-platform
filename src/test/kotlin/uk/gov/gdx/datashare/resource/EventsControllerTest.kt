package uk.gov.gdx.datashare.resource

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.service.*
import java.time.LocalDateTime
import java.util.*

class EventsControllerTest {
  private val eventDataService = mockk<EventDataService>()
  private val dataReceiverService = mockk<DataReceiverService>()
  private val meterRegistry = mockk<MeterRegistry>()
  private val callsToPollCounter = mockk<Counter>()
  private val ingestedEventsCounter = mockk<Counter>()

  private val underTest: EventsController

  init {
    every { meterRegistry.counter("API_CALLS.CallsToPoll", *anyVararg()) }.returns(callsToPollCounter)
    every { meterRegistry.counter("API_CALLS.IngestedEvents", *anyVararg()) }.returns(ingestedEventsCounter)
    every { callsToPollCounter.increment() }.returns(Unit)
    every { ingestedEventsCounter.increment() }.returns(Unit)
    underTest = EventsController(eventDataService, dataReceiverService, meterRegistry)
  }

  @Test
  fun `getEventsStatus gets events status`() {
    runBlocking {
      val startTime = LocalDateTime.now().minusHours(1)
      val endTime = LocalDateTime.now().plusHours(1)
      val eventStatuses = flowOf(
        EventStatus(
          eventType = "DEATH_NOTIFICATION",
          count = 123
        ),
        EventStatus(
          eventType = "LIFE_EVENT",
          count = 456
        )
      )

      coEvery { eventDataService.getEventsStatus(startTime, endTime) }.returns(eventStatuses)

      val eventStatusesOutput = underTest.getEventsStatus(startTime, endTime)

      assertThat(eventStatusesOutput).hasSize(2)
      assertThat(eventStatusesOutput).isEqualTo(eventStatuses.toList())
    }
  }

  @Test
  fun `getEvents gets events`() {
    runBlocking {
      val eventTypes = listOf("DEATH_NOTIFICATION")
      val startTime = LocalDateTime.now().minusHours(1)
      val endTime = LocalDateTime.now().plusHours(1)
      val events = flowOf(
        EventNotification(
          eventId = UUID.randomUUID(),
          eventType = "DEATH_NOTIFICATION",
          sourceId = UUID.randomUUID().toString(),
          eventData = DeathNotificationDetails(
            firstName = "Bob"
          )
        ),
        EventNotification(
          eventId = UUID.randomUUID(),
          eventType = "DEATH_NOTIFICATION",
          sourceId = UUID.randomUUID().toString(),
          eventData = DeathNotificationDetails(
            firstName = "Bob",
            lastName = "Smith"
          )
        ),
      )

      coEvery { eventDataService.getEvents(eventTypes, startTime, endTime) }.returns(events)

      val eventsOutput = underTest.getEvents(eventTypes, startTime, endTime)

      assertThat(eventsOutput).hasSize(2)
      assertThat(eventsOutput).isEqualTo(events.toList())
      verify(exactly = 1) { callsToPollCounter.increment() }
    }
  }

  @Test
  fun `publishEvent sends event to processor`() {
    runBlocking {
      val event = EventToPublish(
        eventType = "DEATH_NOTIFICATION",
        eventTime = LocalDateTime.now(),
        id = "123456789",
        eventDetails = "{\"firstName\":\"Bob\"}"
      )

      coEvery { dataReceiverService.sendToDataProcessor(any()) }.returns(Unit)

      underTest.publishEvent(event)

      verify(exactly = 1) { ingestedEventsCounter.increment() }
      coVerify(exactly = 1) { dataReceiverService.sendToDataProcessor(event) }
    }
  }

  @Test
  fun `deleteEvent deletes event`() {
    runBlocking {
      val eventId = UUID.randomUUID()

      coEvery { eventDataService.deleteEvent(any()) }.returns(Unit)

      underTest.deleteEvent(eventId)

      coVerify(exactly = 1) { eventDataService.deleteEvent(eventId) }
    }
  }
}