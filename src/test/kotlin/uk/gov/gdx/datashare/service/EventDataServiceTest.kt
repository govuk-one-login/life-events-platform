package uk.gov.gdx.datashare.service

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
    every { authenticationFacade.getUsername() }.returns(clientId)
  }

  @Test
  fun `getEventsStatus gets EventStatuses for client`() {
    val startTime = LocalDateTime.now().minusHours(1)
    val endTime = LocalDateTime.now().plusHours(1)

    every { consumerSubscriptionRepository.findAllByClientId(clientId) }.returns(consumerSubscriptions)
    every {
      eventDataRepository.findAllByConsumerSubscription(
        deathNotificationSubscription.id,
        startTime,
        endTime,
      )
    }.returns(deathEvents)
    every {
      eventDataRepository.findAllByConsumerSubscription(
        lifeEventSubscription.id,
        startTime,
        endTime,
      )
    }.returns(lifeEvents)
    every {
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

  @Test
  fun `getEventsStatus uses default start and end time if null passed in`() {
    val fallbackStartTime = LocalDateTime.now().minusHours(1)
    val fallbackEndTime = LocalDateTime.now()

    every { dateTimeHandler.defaultStartTime() }.returns(fallbackStartTime)
    every { dateTimeHandler.now() }.returns(fallbackEndTime)

    every { consumerSubscriptionRepository.findAllByClientId(clientId) }.returns(
      listOf(
        deathNotificationSubscription,
      ),
    )
    every {
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
    verify(exactly = 1) {
      eventDataRepository.findAllByConsumerSubscription(
        deathNotificationSubscription.id,
        fallbackStartTime,
        fallbackEndTime,
      )
    }
  }

  @Test
  fun `getEvent gets Event for client`() {
    val event = deathEvents.first()
    val deathNotificationDetails = DeathNotificationDetails(
      firstNames = "Alice",
      lastName = "Smith",
      address = deathNotificationSubscription.id.toString(),
    )

    every { eventDataRepository.findByClientIdAndId(clientId, event.id) }.returns(event)
    every { consumerSubscriptionRepository.findByEventId(event.id) }.returns(deathNotificationSubscription)
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

  @Test
  fun `getEvent for event that does not exist for client, throws`() {
    val event = deathEvents.first()

    every { eventDataRepository.findByClientIdAndId(clientId, event.id) }.returns(null)

    val exception = assertThrows<EventNotFoundException> { underTest.getEvent(event.id) }

    assertThat(exception.message).isEqualTo("Event ${event.id} not found for polling client $clientId")
  }

  @Test
  fun `getEvent for subscription that does not exist for client, throws`() {
    val event = deathEvents.first()

    every { eventDataRepository.findByClientIdAndId(clientId, event.id) }.returns(event)
    every { consumerSubscriptionRepository.findByEventId(event.id) }.returns(null)

    val exception = assertThrows<ConsumerSubscriptionNotFoundException> { underTest.getEvent(event.id) }

    assertThat(exception.message).isEqualTo("Consumer subscription not found for event ${event.id}")
  }

  @Test
  fun `getEvents gets Events for client`() {
    val eventTypes = listOf("DEATH_NOTIFICATION")
    val startTime = LocalDateTime.now().minusHours(1)
    val endTime = LocalDateTime.now().plusHours(1)
    val deathNotificationDetails = DeathNotificationDetails(
      firstNames = "Alice",
      lastName = "Smith",
      address = deathNotificationSubscription.id.toString(),
    )

    every { consumerSubscriptionRepository.findAllByEventTypesAndClientId(clientId, eventTypes) }
      .returns(listOf(deathNotificationSubscription))
    every {
      eventDataRepository.findPageByConsumerSubscriptions(
        listOf(deathNotificationSubscription.id),
        startTime,
        endTime,
        10,
        0,
      )
    }.returns(deathEvents)
    every {
      eventDataRepository.countByConsumerSubscriptions(
        listOf(deathNotificationSubscription.id),
        startTime,
        endTime,
      )
    }.returns(deathEvents.count())
    val dataPayload = deathEvents.toList()[0].dataPayload!!
    every { deathNotificationService.mapDeathNotification(dataPayload) }.returns(deathNotificationDetails)

    val eventsOutput = underTest.getEvents(eventTypes, startTime, endTime, 0, 10)

    assertThat(eventsOutput.eventModels).isEqualTo(
      deathEvents.map {
        EventNotification(
          eventId = it.id,
          eventType = "DEATH_NOTIFICATION",
          sourceId = it.dataId,
          dataIncluded = true,
          enrichmentFields = "a,b,c",
          eventData = deathNotificationDetails,
        )
      }.toList(),
    )
    assertThat(eventsOutput.count).isEqualTo(deathEvents.count())
  }

  @Test
  fun `getEvents returns the full count when paginated`() {
    val eventTypes = listOf("DEATH_NOTIFICATION")
    val startTime = LocalDateTime.now().minusHours(1)
    val endTime = LocalDateTime.now().plusHours(1)
    val deathNotificationDetails = DeathNotificationDetails(
      firstNames = "Alice",
      lastName = "Smith",
      address = deathNotificationSubscription.id.toString(),
    )
    val totalEventCount = 156

    every { consumerSubscriptionRepository.findAllByEventTypesAndClientId(clientId, eventTypes) }
      .returns(listOf(deathNotificationSubscription))
    every {
      eventDataRepository.findPageByConsumerSubscriptions(
        listOf(deathNotificationSubscription.id),
        startTime,
        endTime,
        10,
        0,
      )
    }.returns(deathEvents)
    every {
      eventDataRepository.countByConsumerSubscriptions(
        listOf(deathNotificationSubscription.id),
        startTime,
        endTime,
      )
    }.returns(totalEventCount)
    val dataPayload = deathEvents.toList()[0].dataPayload!!
    every { deathNotificationService.mapDeathNotification(dataPayload) }.returns(deathNotificationDetails)

    val eventsOutput = underTest.getEvents(eventTypes, startTime, endTime, 0, 10)

    assertThat(eventsOutput.eventModels).isEqualTo(
      deathEvents.map {
        EventNotification(
          eventId = it.id,
          eventType = "DEATH_NOTIFICATION",
          sourceId = it.dataId,
          dataIncluded = true,
          enrichmentFields = "a,b,c",
          eventData = deathNotificationDetails,
        )
      }.toList(),
    )
    assertThat(eventsOutput.count).isEqualTo(totalEventCount)
  }

  @Test
  fun `getEvents gets thin events for client`() {
    val eventTypes = listOf("DEATH_NOTIFICATION")
    val startTime = LocalDateTime.now().minusHours(1)
    val endTime = LocalDateTime.now().plusHours(1)

    every { consumerSubscriptionRepository.findAllByEventTypesAndClientId(clientId, eventTypes) }
      .returns(listOf(thinDeathNotificationSubscription))
    every {
      eventDataRepository.findPageByConsumerSubscriptions(
        listOf(thinDeathNotificationSubscription.id),
        startTime,
        endTime,
        10,
        0,
      )
    }.returns(thinDeathEvents)
    every {
      eventDataRepository.countByConsumerSubscriptions(
        listOf(thinDeathNotificationSubscription.id),
        startTime,
        endTime,
      )
    }.returns(thinDeathEvents.count())

    val eventsOutput = underTest.getEvents(eventTypes, startTime, endTime, 0, 10)

    assertThat(eventsOutput.eventModels).isEqualTo(
      thinDeathEvents.map {
        EventNotification(
          eventId = it.id,
          eventType = "DEATH_NOTIFICATION",
          sourceId = it.dataId,
          dataIncluded = false,
          enrichmentFields = "a,b,c",
          eventData = null,
        )
      }.toList(),
    )
    assertThat(eventsOutput.count).isEqualTo(thinDeathEvents.count())
  }

  @Test
  fun `getEvents uses default start, end time, and eventTypes if null passed in`() {
    val fallbackStartTime = LocalDateTime.now().minusHours(1)
    val fallbackEndTime = LocalDateTime.now()

    every { dateTimeHandler.defaultStartTime() }.returns(fallbackStartTime)
    every { dateTimeHandler.now() }.returns(fallbackEndTime)

    val deathNotificationDetails = DeathNotificationDetails(
      firstNames = "Bob",
      lastName = "Smith",
      address = deathNotificationSubscription.id.toString(),
    )

    every { consumerSubscriptionRepository.findAllByClientId(clientId) }
      .returns(listOf(deathNotificationSubscription))
    every {
      eventDataRepository.findPageByConsumerSubscriptions(
        listOf(deathNotificationSubscription.id),
        fallbackStartTime,
        fallbackEndTime,
        10,
        0,
      )
    }.returns(extraDeathEvents)
    every {
      eventDataRepository.countByConsumerSubscriptions(
        listOf(deathNotificationSubscription.id),
        fallbackStartTime,
        fallbackEndTime,
      )
    }.returns(extraDeathEvents.count())
    val dataPayload = extraDeathEvents.toList()[0].dataPayload!!
    every { deathNotificationService.mapDeathNotification(dataPayload) }.returns(deathNotificationDetails)

    val eventStatusOutput = underTest.getEvents(null, null, null, 0, 10)

    assertThat(eventStatusOutput.eventModels).isEqualTo(
      extraDeathEvents.map {
        EventNotification(
          eventId = it.id,
          eventType = "DEATH_NOTIFICATION",
          sourceId = it.dataId,
          dataIncluded = true,
          enrichmentFields = "a,b,c",
          eventData = deathNotificationDetails,
        )
      }.toList(),
    )
    assertThat(eventStatusOutput.count).isEqualTo(extraDeathEvents.count())
  }

  @Test
  fun `deleteEvent deletes event`() {
    val event = EventData(
      consumerSubscriptionId = UUID.randomUUID(),
      datasetId = UUID.randomUUID().toString(),
      dataId = "HMPO",
      dataPayload = null,
    )
    every { eventDataRepository.findByClientIdAndId(clientId, event.id) }.returns(event)
    every { consumerSubscriptionRepository.findByEventId(event.id) }.returns(
      deathNotificationSubscription,
    )

    every { eventDataRepository.softDeleteById(event.id) }.returns(Unit)
    every { dateTimeHandler.now() }.returns(LocalDateTime.now())

    underTest.deleteEvent(event.id)

    verify(exactly = 1) { eventDataRepository.softDeleteById(event.id) }
    verify(exactly = 1) { eventDeletedCounter.increment() }
  }

  @Test
  fun `deleteEvent throws if event not found for client`() {
    val eventId = UUID.randomUUID()
    every { eventDataRepository.findByClientIdAndId(clientId, eventId) }.returns(null)

    val exception = assertThrows<EventNotFoundException> {
      underTest.deleteEvent(eventId)
    }

    assertThat(exception.message).isEqualTo("Event $eventId not found for callback client $clientId")

    verify(exactly = 0) { eventDataRepository.softDeleteById(any()) }
    verify(exactly = 0) { eventDeletedCounter.increment() }
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
  private val consumerSubscriptions = listOf(deathNotificationSubscription, lifeEventSubscription)
  private val deathEvents = getEvents(4, "Alice", deathNotificationSubscription.id)
  private val thinDeathEvents = getEvents(4, "Alice", thinDeathNotificationSubscription.id)
  private val extraDeathEvents = getEvents(10, "Bob", deathNotificationSubscription.id)
  private val lifeEvents = getEvents(7, "Charlie", lifeEventSubscription.id)

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
