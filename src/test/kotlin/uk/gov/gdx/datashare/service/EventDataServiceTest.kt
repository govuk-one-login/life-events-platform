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
import uk.gov.gdx.datashare.repository.EgressEventData
import uk.gov.gdx.datashare.repository.EgressEventDataRepository
import uk.gov.gdx.datashare.repository.IngressEventDataRepository
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

class EventDataServiceTest {
  private val authenticationFacade = mockk<AuthenticationFacade>()
  private val consumerSubscriptionRepository = mockk<ConsumerSubscriptionRepository>()
  private val egressEventDataRepository = mockk<EgressEventDataRepository>()
  private val ingressEventDataRepository = mockk<IngressEventDataRepository>()
  private val deathNotificationService = mockk<DeathNotificationService>()
  private val dateTimeHandler = mockk<DateTimeHandler>()
  private val meterRegistry = mockk<MeterRegistry>()
  private val dataCreationToDeletionTimer = mockk<Timer>()
  private val egressEventDeletedCounter = mockk<Counter>()

  private val underTest: EventDataService

  init {
    every { meterRegistry.timer("DATA_PROCESSING.TimeFromCreationToDeletion", *anyVararg()) }.returns(
      dataCreationToDeletionTimer,
    )
    every { dataCreationToDeletionTimer.record(any<Duration>()) }.returns(Unit)
    every {
      meterRegistry.counter(
        "EVENT_ACTION.EgressEventDeleted",
        *anyVararg(),
      )
    }.returns(egressEventDeletedCounter)
    every { egressEventDeletedCounter.increment() }.returns(Unit)
    underTest = EventDataService(
      authenticationFacade,
      consumerSubscriptionRepository,
      egressEventDataRepository,
      ingressEventDataRepository,
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
        egressEventDataRepository.findAllByConsumerSubscription(
          deathNotificationSubscription.id,
          startTime,
          endTime,
        )
      }.returns(deathEvents)
      coEvery {
        egressEventDataRepository.findAllByConsumerSubscription(
          lifeEventSubscription.id,
          startTime,
          endTime,
        )
      }.returns(lifeEvents)
      coEvery {
        egressEventDataRepository.findAllByConsumerSubscription(
          UUID.randomUUID(),
          startTime,
          endTime,
        )
      }.returns(extraDeathEvents)

      val eventStatusOutput = underTest.getEventsStatus(startTime, endTime).toList()

      assertThat(eventStatusOutput).isEqualTo(
        listOf(
          EventStatus(eventType = deathNotificationSubscription.ingressEventType, count = 4),
          EventStatus(eventType = lifeEventSubscription.ingressEventType, count = 7),
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
        egressEventDataRepository.findAllByConsumerSubscription(
          deathNotificationSubscription.id,
          fallbackStartTime,
          fallbackEndTime,
        )
      }.returns(deathEvents)

      val eventStatusOutput = underTest.getEventsStatus(null, null).toList()

      assertThat(eventStatusOutput).isEqualTo(
        listOf(
          EventStatus(eventType = deathNotificationSubscription.ingressEventType, count = 4),
        ),
      )
      coVerify(exactly = 1) {
        egressEventDataRepository.findAllByConsumerSubscription(
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

      coEvery { egressEventDataRepository.findByClientIdAndId(clientId, event.id) }.returns(event)
      coEvery { consumerSubscriptionRepository.findByEgressEventId(event.id) }.returns(deathNotificationSubscription)
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

      coEvery { egressEventDataRepository.findByClientIdAndId(clientId, event.id) }.returns(null)

      val exception = assertThrows<EventNotFoundException> { underTest.getEvent(event.id) }

      assertThat(exception.message).isEqualTo("Egress event ${event.id} not found for polling client $clientId")
    }
  }

  @Test
  fun `getEvent for subscription that does not exist for client, throws`() {
    runBlocking {
      val event = deathEvents.first()

      coEvery { egressEventDataRepository.findByClientIdAndId(clientId, event.id) }.returns(event)
      coEvery { consumerSubscriptionRepository.findByEgressEventId(event.id) }.returns(null)

      val exception = assertThrows<ConsumerSubscriptionNotFoundException> { underTest.getEvent(event.id) }

      assertThat(exception.message).isEqualTo("Consumer subscription not found for egress event ${event.id}")
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

      coEvery { consumerSubscriptionRepository.findAllByIngressEventTypesAndClientId(clientId, eventTypes) }
        .returns(flowOf(deathNotificationSubscription))
      coEvery {
        egressEventDataRepository.findAllByConsumerSubscriptions(
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
      val deathNotificationDetails = DeathNotificationDetails(
        firstName = "Alice",
        lastName = "Smith",
        address = thinDeathNotificationSubscription.id.toString(),
      )

      coEvery { consumerSubscriptionRepository.findAllByIngressEventTypesAndClientId(clientId, eventTypes) }
        .returns(flowOf(thinDeathNotificationSubscription))
      coEvery {
        egressEventDataRepository.findAllByConsumerSubscriptions(
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
        egressEventDataRepository.findAllByConsumerSubscriptions(
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
  fun `deleteEvent deletes egress event`() {
    runBlocking {
      val egressEvent = EgressEventData(
        consumerSubscriptionId = UUID.randomUUID(),
        ingressEventId = UUID.randomUUID(),
        datasetId = UUID.randomUUID().toString(),
        dataId = "HMPO",
        dataPayload = null,
      )
      coEvery { egressEventDataRepository.findByClientIdAndId(clientId, egressEvent.id) }.returns(egressEvent)
      coEvery { consumerSubscriptionRepository.findByEgressEventId(egressEvent.id) }.returns(
        deathNotificationSubscription,
      )
      coEvery { egressEventDataRepository.findAllByIngressEventId(egressEvent.ingressEventId) }.returns(
        getEgressEvents(
          10,
        ).asFlow(),
      )

      coEvery { egressEventDataRepository.delete(egressEvent) }.returns(Unit)

      underTest.deleteEvent(egressEvent.id)

      coVerify(exactly = 1) { egressEventDataRepository.delete(egressEvent) }
      coVerify(exactly = 0) { ingressEventDataRepository.deleteById(any()) }
      coVerify(exactly = 1) { egressEventDeletedCounter.increment() }
    }
  }

  @Test
  fun `deleteEvent deletes ingress event if no egress events left`() {
    runBlocking {
      val egressEvent = EgressEventData(
        consumerSubscriptionId = UUID.randomUUID(),
        ingressEventId = UUID.randomUUID(),
        datasetId = UUID.randomUUID().toString(),
        dataId = "HMPO",
        dataPayload = null,
      )
      coEvery { egressEventDataRepository.findByClientIdAndId(clientId, egressEvent.id) }.returns(egressEvent)
      coEvery { consumerSubscriptionRepository.findByEgressEventId(egressEvent.id) }.returns(
        deathNotificationSubscription,
      )
      coEvery { egressEventDataRepository.findAllByIngressEventId(egressEvent.ingressEventId) }.returns(emptyList<EgressEventData>().asFlow())

      coEvery { egressEventDataRepository.delete(egressEvent) }.returns(Unit)
      coEvery { ingressEventDataRepository.deleteById(egressEvent.ingressEventId) }.returns(Unit)

      underTest.deleteEvent(egressEvent.id)

      coVerify(exactly = 1) { egressEventDataRepository.delete(egressEvent) }
      coVerify(exactly = 1) { ingressEventDataRepository.deleteById(egressEvent.ingressEventId) }
      coVerify(exactly = 1) { egressEventDeletedCounter.increment() }
    }
  }

  @Test
  fun `deleteEvent throws if egress event not found for client`() {
    runBlocking {
      val egressEventId = UUID.randomUUID()
      coEvery { egressEventDataRepository.findByClientIdAndId(clientId, egressEventId) }.returns(null)

      val exception = assertThrows<EventNotFoundException> {
        underTest.deleteEvent(egressEventId)
      }

      assertThat(exception.message).isEqualTo("Egress event $egressEventId not found for callback client $clientId")

      coVerify(exactly = 0) { egressEventDataRepository.deleteById(any()) }
      coVerify(exactly = 0) { ingressEventDataRepository.deleteById(any()) }
      coVerify(exactly = 0) { egressEventDeletedCounter.increment() }
    }
  }

  private val clientId = "ClientId"
  private val deathNotificationSubscription = ConsumerSubscription(
    consumerId = UUID.randomUUID(),
    oauthClientId = clientId,
    ingressEventType = "DEATH_NOTIFICATION",
    enrichmentFields = "a,b,c",
    enrichmentFieldsIncludedInPoll = true,
  )
  private val thinDeathNotificationSubscription = ConsumerSubscription(
    consumerId = UUID.randomUUID(),
    oauthClientId = clientId,
    ingressEventType = "DEATH_NOTIFICATION",
    enrichmentFields = "a,b,c",
    enrichmentFieldsIncludedInPoll = false,
  )
  private val lifeEventSubscription = ConsumerSubscription(
    consumerId = UUID.randomUUID(),
    oauthClientId = clientId,
    ingressEventType = "LIFE_EVENT",
    enrichmentFields = "a,b,c",
    enrichmentFieldsIncludedInPoll = true,
  )
  private val consumerSubscriptions = flowOf(deathNotificationSubscription, lifeEventSubscription)
  private val deathEvents = getEgressEvents(4, "Alice", deathNotificationSubscription.id).asFlow()
  private val thinDeathEvents = getEgressEvents(4, "Alice", thinDeathNotificationSubscription.id).asFlow()
  private val extraDeathEvents = getEgressEvents(10, "Bob", deathNotificationSubscription.id).asFlow()
  private val lifeEvents = getEgressEvents(7, "Charlie", lifeEventSubscription.id).asFlow()

  private fun getEgressEvents(
    count: Int,
    firstName: String = "Alice",
    subscriptionId: UUID = UUID.randomUUID(),
  ): List<EgressEventData> =
    List(count) {
      EgressEventData(
        consumerSubscriptionId = subscriptionId,
        ingressEventId = UUID.randomUUID(),
        datasetId = UUID.randomUUID().toString(),
        dataId = "HMPO",
        dataPayload = "{\"firstName\":\"$firstName\",\"lastName\":\"Smith\",\"age\":12,\"address\":\"$subscriptionId\"}",
      )
    }
}
