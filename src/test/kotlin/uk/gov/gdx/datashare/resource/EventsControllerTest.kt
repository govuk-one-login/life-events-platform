package uk.gov.gdx.datashare.resource

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

  @ParameterizedTest
  @MethodSource("provideLocalDateTimes")
  fun `getEventsStatus gets events status`(startTime: LocalDateTime?, endTime: LocalDateTime?) {
    runBlocking {
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
