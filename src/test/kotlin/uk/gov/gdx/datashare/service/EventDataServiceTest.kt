package uk.gov.gdx.datashare.service

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.gdx.datashare.config.AuthenticationFacade
import uk.gov.gdx.datashare.config.ConsumerSubscriptionNotFoundException
import uk.gov.gdx.datashare.config.DateTimeHandler
import uk.gov.gdx.datashare.config.EventNotFoundException
import uk.gov.gdx.datashare.repository.ConsumerSubscription
import uk.gov.gdx.datashare.repository.ConsumerSubscriptionRepository
import uk.gov.gdx.datashare.repository.EventData
import uk.gov.gdx.datashare.repository.EventDataRepository
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

class EventDataServiceTest {
  private val authenticationFacade = mockk<AuthenticationFacade>()
  private val consumerSubscriptionRepository = mockk<ConsumerSubscriptionRepository>()
  private val eventDataRepository = mockk<EventDataRepository>()
  private val deathNotificationService = mockk<DeathNotificationService>()
  private val dateTimeHandler = mockk<DateTimeHandler>()
  private val meterRegistry = mockk<MeterRegistry>()
  private val dataCreationToDeletionTimer = mockk<Timer>()
  private val eventDeletedCounter = mockk<Counter>()

  private val underTest: EventDataService

  init {
    every { meterRegistry.timer("DATA_PROCESSING.TimeFromCreationToDeletion", *anyVararg()) }.returns(
      dataCreationToDeletionTimer,
    )
    every { dataCreationToDeletionTimer.record(any<Duration>()) }.returns(Unit)
    every {
      meterRegistry.counter(
        "EVENT_ACTION.EventDeleted",
        *anyVararg(),
      )
    }.returns(eventDeletedCounter)
    every { eventDeletedCounter.increment() }.returns(Unit)
    underTest = EventDataService(
      authenticationFacade,
      consumerSubscriptionRepository,
      eventDataRepository,
      deathNotificationService,
      dateTimeHandler,
      meterRegistry,
    )
  }

  @BeforeEach
  fun setup() {
    coEvery { authenticationFacade.getUsername() }.returns(clientId)
  }

  @Test
  fun `getEventsStatus gets EventStatuses for client`() {
    runBlocking {
      val startTime = LocalDateTime.now().minusHours(1)
      val endTime = LocalDateTime.now().plusHours(1)

      coEvery { consumerSubscriptionRepository.findAllByClientId(clientId) }.returns(consumerSubscriptions)
      coEvery {
        eventDataRepository.findAllByConsumerSubscription(
          deathNotificationSubscription.id,
          startTime,
          endTime,
        )
      }.returns(deathEvents)
      coEvery {
        eventDataRepository.findAllByConsumerSubscription(
          lifeEventSubscription.id,
          startTime,
          endTime,
        )
      }.returns(lifeEvents)
      coEvery {
        eventDataRepository.findAllByConsumerSubscription(
          UUID.randomUUID(),
          startTime,
          endTime,
        )
      }.returns(extraDeathEvents)

      val eventStatusOutput = underTest.getEventsStatus(startTime, endTime).toList()

      assertThat(eventStatusOutput).isEqualTo(
        listOf(
          EventStatus(eventType = deathNotificationSubscription.eventType, count = 4),
          EventStatus(eventType = lifeEventSubscription.eventType, count = 7),
        ),
      )
    }
  }

  @Test
  fun `getEventsStatus uses default start and end time if null passed in`() {
    runBlocking {
      val fallbackStartTime = LocalDateTime.now().minusHours(1)
      val fallbackEndTime = LocalDateTime.now()

      every { dateTimeHandler.defaultStartTime() }.returns(fallbackStartTime)
      every { dateTimeHandler.now() }.returns(fallbackEndTime)

      coEvery { consumerSubscriptionRepository.findAllByClientId(clientId) }.returns(
        flowOf(
          deathNotificationSubscription,
        ),
      )
      coEvery {
        eventDataRepository.findAllByConsumerSubscription(
          deathNotificationSubscription.id,
          fallbackStartTime,
          fallbackEndTime,
        )
      }.returns(deathEvents)

      val eventStatusOutput = underTest.getEventsStatus(null, null).toList()

      assertThat(eventStatusOutput).isEqualTo(
        listOf(
          EventStatus(eventType = deathNotificationSubscription.eventType, count = 4),
        ),
      )
      coVerify(exactly = 1) {
        eventDataRepository.findAllByConsumerSubscription(
          deathNotificationSubscription.id,
          fallbackStartTime,
          fallbackEndTime,
        )
      }
    }
  }

  @Test
  fun `getEvent gets Event for client`() {
    runBlocking {
      val event = deathEvents.first()
      val deathNotificationDetails = DeathNotificationDetails(
        firstName = "Alice",
        lastName = "Smith",
        address = deathNotificationSubscription.id.toString(),
      )

      coEvery { eventDataRepository.findByClientIdAndId(clientId, event.id) }.returns(event)
      coEvery { consumerSubscriptionRepository.findByEventId(event.id) }.returns(deathNotificationSubscription)
      every { deathNotificationService.mapDeathNotification(event.dataPayload!!) }.returns(deathNotificationDetails)

      val eventOutput = underTest.getEvent(event.id)

      assertThat(eventOutput).isEqualTo(
        EventNotification(
          eventId = event.id,
          eventType = "DEATH_NOTIFICATION",
          sourceId = event.dataId,
          eventData = deathNotificationDetails,
        ),
      )
    }
  }

  @Test
  fun `getEvent for event that does not exist for client, throws`() {
    runBlocking {
      val event = deathEvents.first()

      coEvery { eventDataRepository.findByClientIdAndId(clientId, event.id) }.returns(null)

      val exception = assertThrows<EventNotFoundException> { underTest.getEvent(event.id) }

      assertThat(exception.message).isEqualTo("Event ${event.id} not found for polling client $clientId")
    }
  }

  @Test
  fun `getEvent for subscription that does not exist for client, throws`() {
    runBlocking {
      val event = deathEvents.first()

      coEvery { eventDataRepository.findByClientIdAndId(clientId, event.id) }.returns(event)
      coEvery { consumerSubscriptionRepository.findByEventId(event.id) }.returns(null)

      val exception = assertThrows<ConsumerSubscriptionNotFoundException> { underTest.getEvent(event.id) }

      assertThat(exception.message).isEqualTo("Consumer subscription not found for event ${event.id}")
    }
  }

  @Test
  fun `getEvents gets Events for client`() {
    runBlocking {
      val eventTypes = listOf("DEATH_NOTIFICATION")
      val startTime = LocalDateTime.now().minusHours(1)
      val endTime = LocalDateTime.now().plusHours(1)
      val deathNotificationDetails = DeathNotificationDetails(
        firstName = "Alice",
        lastName = "Smith",
        address = deathNotificationSubscription.id.toString(),
      )

      coEvery { consumerSubscriptionRepository.findAllByEventTypesAndClientId(clientId, eventTypes) }
        .returns(flowOf(deathNotificationSubscription))
      coEvery {
        eventDataRepository.findAllByConsumerSubscriptions(
          listOf(deathNotificationSubscription.id),
          startTime,
          endTime,
        )
      }.returns(deathEvents)
      val dataPayload = deathEvents.toList()[0].dataPayload!!
      every { deathNotificationService.mapDeathNotification(dataPayload) }.returns(deathNotificationDetails)

      val eventsOutput = underTest.getEvents(eventTypes, startTime, endTime).toList()

      assertThat(eventsOutput).isEqualTo(
        deathEvents.map {
          EventNotification(
            eventId = it.id,
            eventType = "DEATH_NOTIFICATION",
            sourceId = it.dataId,
            eventData = deathNotificationDetails,
          )
        }.toList(),
      )
    }
  }

  @Test
  fun `getEvents gets thin events for client`() {
    runBlocking {
      val eventTypes = listOf("DEATH_NOTIFICATION")
      val startTime = LocalDateTime.now().minusHours(1)
      val endTime = LocalDateTime.now().plusHours(1)

      coEvery { consumerSubscriptionRepository.findAllByEventTypesAndClientId(clientId, eventTypes) }
        .returns(flowOf(thinDeathNotificationSubscription))
      coEvery {
        eventDataRepository.findAllByConsumerSubscriptions(
          listOf(thinDeathNotificationSubscription.id),
          startTime,
          endTime,
        )
      }.returns(thinDeathEvents)

      val eventsOutput = underTest.getEvents(eventTypes, startTime, endTime).toList()

      assertThat(eventsOutput).isEqualTo(
        thinDeathEvents.map {
          EventNotification(
            eventId = it.id,
            eventType = "DEATH_NOTIFICATION",
            sourceId = it.dataId,
            eventData = null,
          )
        }.toList(),
      )
    }
  }

  @Test
  fun `getEvents uses default start, end time, and eventTypes if null passed in`() {
    runBlocking {
      val fallbackStartTime = LocalDateTime.now().minusHours(1)
      val fallbackEndTime = LocalDateTime.now()

      every { dateTimeHandler.defaultStartTime() }.returns(fallbackStartTime)
      every { dateTimeHandler.now() }.returns(fallbackEndTime)

      val deathNotificationDetails = DeathNotificationDetails(
        firstName = "Bob",
        lastName = "Smith",
        address = deathNotificationSubscription.id.toString(),
      )

      coEvery { consumerSubscriptionRepository.findAllByClientId(clientId) }
        .returns(flowOf(deathNotificationSubscription))
      coEvery {
        eventDataRepository.findAllByConsumerSubscriptions(
          listOf(deathNotificationSubscription.id),
          fallbackStartTime,
          fallbackEndTime,
        )
      }.returns(extraDeathEvents)
      val dataPayload = extraDeathEvents.toList()[0].dataPayload!!
      every { deathNotificationService.mapDeathNotification(dataPayload) }.returns(deathNotificationDetails)

      val eventStatusOutput = underTest.getEvents(null, null, null).toList()

      assertThat(eventStatusOutput).isEqualTo(
        extraDeathEvents.map {
          EventNotification(
            eventId = it.id,
            eventType = "DEATH_NOTIFICATION",
            sourceId = it.dataId,
            eventData = deathNotificationDetails,
          )
        }.toList(),
      )
    }
  }

  @Test
  fun `deleteEvent deletes event`() {
    runBlocking {
      val event = EventData(
        consumerSubscriptionId = UUID.randomUUID(),
        datasetId = UUID.randomUUID().toString(),
        dataId = "HMPO",
        dataPayload = null,
      )
      coEvery { eventDataRepository.findByClientIdAndId(clientId, event.id) }.returns(event)
      coEvery { consumerSubscriptionRepository.findByEventId(event.id) }.returns(
        deathNotificationSubscription,
      )

      coEvery { eventDataRepository.delete(event) }.returns(Unit)

      underTest.deleteEvent(event.id)

      coVerify(exactly = 1) { eventDataRepository.delete(event) }
      coVerify(exactly = 1) { eventDeletedCounter.increment() }
    }
  }

  @Test
  fun `deleteEvent throws if event not found for client`() {
    runBlocking {
      val eventId = UUID.randomUUID()
      coEvery { eventDataRepository.findByClientIdAndId(clientId, eventId) }.returns(null)

      val exception = assertThrows<EventNotFoundException> {
        underTest.deleteEvent(eventId)
      }

      assertThat(exception.message).isEqualTo("Event $eventId not found for callback client $clientId")

      coVerify(exactly = 0) { eventDataRepository.deleteById(any()) }
      coVerify(exactly = 0) { eventDeletedCounter.increment() }
    }
  }

  private val clientId = "ClientId"
  private val deathNotificationSubscription = ConsumerSubscription(
    consumerId = UUID.randomUUID(),
    oauthClientId = clientId,
    eventType = "DEATH_NOTIFICATION",
    enrichmentFields = "a,b,c",
    enrichmentFieldsIncludedInPoll = true,
  )
  private val thinDeathNotificationSubscription = ConsumerSubscription(
    consumerId = UUID.randomUUID(),
    oauthClientId = clientId,
    eventType = "DEATH_NOTIFICATION",
    enrichmentFields = "a,b,c",
    enrichmentFieldsIncludedInPoll = false,
  )
  private val lifeEventSubscription = ConsumerSubscription(
    consumerId = UUID.randomUUID(),
    oauthClientId = clientId,
    eventType = "LIFE_EVENT",
    enrichmentFields = "a,b,c",
    enrichmentFieldsIncludedInPoll = true,
  )
  private val consumerSubscriptions = flowOf(deathNotificationSubscription, lifeEventSubscription)
  private val deathEvents = getEvents(4, "Alice", deathNotificationSubscription.id).asFlow()
  private val thinDeathEvents = getEvents(4, "Alice", thinDeathNotificationSubscription.id).asFlow()
  private val extraDeathEvents = getEvents(10, "Bob", deathNotificationSubscription.id).asFlow()
  private val lifeEvents = getEvents(7, "Charlie", lifeEventSubscription.id).asFlow()

  private fun getEvents(
    count: Int,
    firstName: String = "Alice",
    subscriptionId: UUID = UUID.randomUUID(),
  ): List<EventData> =
    List(count) {
      EventData(
        consumerSubscriptionId = subscriptionId,
        datasetId = UUID.randomUUID().toString(),
        dataId = "HMPO",
        dataPayload = "{\"firstName\":\"$firstName\",\"lastName\":\"Smith\",\"age\":12,\"address\":\"$subscriptionId\"}",
      )
    }
}
