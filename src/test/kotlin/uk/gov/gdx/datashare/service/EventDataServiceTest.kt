package uk.gov.gdx.datashare.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.webjars.NotFoundException
import uk.gov.gdx.datashare.config.AuthenticationFacade
import uk.gov.gdx.datashare.config.DateTimeHandler
import uk.gov.gdx.datashare.repository.*
import java.time.LocalDateTime
import java.util.*

class EventDataServiceTest {
  private val authenticationFacade = mockk<AuthenticationFacade>()
  private val consumerSubscriptionRepository = mockk<ConsumerSubscriptionRepository>()
  private val egressEventDataRepository = mockk<EgressEventDataRepository>()
  private val ingressEventDataRepository = mockk<IngressEventDataRepository>()
  private val deathNotificationService = mockk<DeathNotificationService>()
  private val dateTimeHandler = mockk<DateTimeHandler>()

  private val underTest = EventDataService(
    authenticationFacade,
    consumerSubscriptionRepository,
    egressEventDataRepository,
    ingressEventDataRepository,
    deathNotificationService,
    dateTimeHandler
  )

  @BeforeEach
  fun setup() {
    coEvery { authenticationFacade.getUsername() }.returns(clientId)
  }

  @Test
  fun `getEventsStatus gets EventStatuses for client`() {
    runBlocking {
      val startTime = LocalDateTime.now().minusHours(1)
      val endTime = LocalDateTime.now().plusHours(1)

      coEvery { consumerSubscriptionRepository.findAllByPollClientId(clientId) }.returns(consumerSubscriptions)
      coEvery {
        egressEventDataRepository.findAllByConsumerSubscription(
          deathNotificationSubscription.id,
          startTime,
          endTime
        )
      }.returns(deathEvents)
      coEvery {
        egressEventDataRepository.findAllByConsumerSubscription(
          lifeEventSubscription.id,
          startTime,
          endTime
        )
      }.returns(lifeEvents)
      coEvery {
        egressEventDataRepository.findAllByConsumerSubscription(
          UUID.randomUUID(),
          startTime,
          endTime
        )
      }.returns(extraDeathEvents)

      val eventStatusOutput = underTest.getEventsStatus(startTime, endTime).toList()

      assertThat(eventStatusOutput).isEqualTo(
        listOf(
          EventStatus(eventType = deathNotificationSubscription.ingressEventType, count = 4),
          EventStatus(eventType = lifeEventSubscription.ingressEventType, count = 7)
        )
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

      coEvery { consumerSubscriptionRepository.findAllByPollClientId(clientId) }.returns(
        flowOf(
          deathNotificationSubscription
        )
      )
      coEvery {
        egressEventDataRepository.findAllByConsumerSubscription(
          deathNotificationSubscription.id,
          fallbackStartTime,
          fallbackEndTime
        )
      }.returns(deathEvents)

      val eventStatusOutput = underTest.getEventsStatus(null, null).toList()

      assertThat(eventStatusOutput).isEqualTo(
        listOf(
          EventStatus(eventType = deathNotificationSubscription.ingressEventType, count = 4)
        )
      )
      coVerify(exactly = 1) {
        egressEventDataRepository.findAllByConsumerSubscription(
          deathNotificationSubscription.id,
          fallbackStartTime,
          fallbackEndTime
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
        address = deathNotificationSubscription.id.toString()
      )

      coEvery { egressEventDataRepository.findByPollClientIdAndId(clientId, event.id) }.returns(event)
      coEvery { consumerSubscriptionRepository.findByEgressEventId(event.id) }.returns(deathNotificationSubscription)
      every { deathNotificationService.mapDeathNotification(event.dataPayload!!) }.returns(deathNotificationDetails)

      val eventOutput = underTest.getEvent(event.id)

      assertThat(eventOutput).isEqualTo(
          EventNotification(
            eventId = event.id,
            eventType = "DEATH_NOTIFICATION",
            sourceId = event.dataId,
            eventData = deathNotificationDetails,
          )
      )
    }
  }

  @Test
  fun `getEvent for event that does not exist for client, throws`() {
    runBlocking {
      val event = deathEvents.first()

      coEvery { egressEventDataRepository.findByPollClientIdAndId(clientId, event.id) }.returns(null)

      val exception = assertThrows<NotFoundException> { underTest.getEvent(event.id) }

      assertThat(exception.message).isEqualTo("Egress event ${event.id} not found for polling client $clientId")
    }
  }

  @Test
  fun `getEvent for subscription that does not exist for client, throws`() {
    runBlocking {
      val event = deathEvents.first()

      coEvery { egressEventDataRepository.findByPollClientIdAndId(clientId, event.id) }.returns(event)
      coEvery { consumerSubscriptionRepository.findByEgressEventId(event.id) }.returns(null)

      val exception = assertThrows<NotFoundException> { underTest.getEvent(event.id) }

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
        address = deathNotificationSubscription.id.toString()
      )

      coEvery { consumerSubscriptionRepository.findAllByIngressEventTypesAndPollClientId(clientId, eventTypes) }
        .returns(flowOf(deathNotificationSubscription))
      coEvery {
        egressEventDataRepository.findAllByConsumerSubscriptions(
          listOf(deathNotificationSubscription.id),
          startTime,
          endTime
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
        }.toList()
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
        address = deathNotificationSubscription.id.toString()
      )

      coEvery { consumerSubscriptionRepository.findAllByPollClientId(clientId) }
        .returns(flowOf(deathNotificationSubscription))
      coEvery {
        egressEventDataRepository.findAllByConsumerSubscriptions(
          listOf(deathNotificationSubscription.id),
          fallbackStartTime,
          fallbackEndTime
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
        }.toList()
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
        dataExpiryTime = LocalDateTime.now(),
        dataPayload = null
      )
      coEvery { egressEventDataRepository.findByPollClientIdAndId(clientId, egressEvent.id) }.returns(egressEvent)
      coEvery { egressEventDataRepository.findAllByIngressEventId(egressEvent.ingressEventId) }.returns(
        getEgressEvents(
          10
        ).asFlow()
      )

      coEvery { egressEventDataRepository.deleteById(egressEvent.id) }.returns(Unit)

      underTest.deleteEvent(egressEvent.id)

      coVerify(exactly = 1) { egressEventDataRepository.deleteById(egressEvent.id) }
      coVerify(exactly = 0) { ingressEventDataRepository.deleteById(any()) }
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
        dataExpiryTime = LocalDateTime.now(),
        dataPayload = null
      )
      coEvery { egressEventDataRepository.findByPollClientIdAndId(clientId, egressEvent.id) }.returns(egressEvent)
      coEvery { egressEventDataRepository.findAllByIngressEventId(egressEvent.ingressEventId) }.returns(emptyList<EgressEventData>().asFlow())

      coEvery { egressEventDataRepository.deleteById(egressEvent.id) }.returns(Unit)
      coEvery { ingressEventDataRepository.deleteById(egressEvent.ingressEventId) }.returns(Unit)

      underTest.deleteEvent(egressEvent.id)

      coVerify(exactly = 1) { egressEventDataRepository.deleteById(egressEvent.id) }
      coVerify(exactly = 1) { ingressEventDataRepository.deleteById(egressEvent.ingressEventId) }
    }
  }

  @Test
  fun `deleteEvent throws if egress event not found for client`() {
    runBlocking {
      val egressEventId = UUID.randomUUID()
      coEvery { egressEventDataRepository.findByPollClientIdAndId(clientId, egressEventId) }.returns(null)

      val exception = assertThrows<NotFoundException> {
        underTest.deleteEvent(egressEventId)
      }

      assertThat(exception.message).isEqualTo("Egress event $egressEventId not found for polling client $clientId")

      coVerify(exactly = 0) { egressEventDataRepository.deleteById(any()) }
      coVerify(exactly = 0) { ingressEventDataRepository.deleteById(any()) }
    }
  }

  private val clientId = "ClientId"
  private val deathNotificationSubscription = ConsumerSubscription(
    consumerId = UUID.randomUUID(),
    pollClientId = clientId,
    ingressEventType = "DEATH_NOTIFICATION",
    enrichmentFields = "a,b,c",
  )
  private val lifeEventSubscription = ConsumerSubscription(
    consumerId = UUID.randomUUID(),
    pollClientId = clientId,
    ingressEventType = "LIFE_EVENT",
    enrichmentFields = "a,b,c",
  )
  private val consumerSubscriptions = flowOf(deathNotificationSubscription, lifeEventSubscription)
  private val deathEvents = getEgressEvents(4, "Alice", deathNotificationSubscription.id).asFlow()
  private val extraDeathEvents = getEgressEvents(10, "Bob", deathNotificationSubscription.id).asFlow()
  private val lifeEvents = getEgressEvents(7, "Charlie", lifeEventSubscription.id).asFlow()

  private fun getEgressEvents(
    count: Int,
    firstName: String = "Alice",
    subscriptionId: UUID = UUID.randomUUID()
  ): List<EgressEventData> =
    List(count) {
      EgressEventData(
        consumerSubscriptionId = subscriptionId,
        ingressEventId = UUID.randomUUID(),
        datasetId = UUID.randomUUID().toString(),
        dataId = "HMPO",
        dataExpiryTime = LocalDateTime.now(),
        dataPayload = "{\"firstName\":\"$firstName\",\"lastName\":\"Smith\",\"age\":12,\"address\":\"$subscriptionId\"}"
      )
    }
}
