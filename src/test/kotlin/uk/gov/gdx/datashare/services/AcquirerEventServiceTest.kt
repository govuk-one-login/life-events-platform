package uk.gov.gdx.datashare.services

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.gdx.datashare.config.AcquirerSubscriptionNotFoundException
import uk.gov.gdx.datashare.config.AuthenticationFacade
import uk.gov.gdx.datashare.config.DateTimeHandler
import uk.gov.gdx.datashare.config.EventNotFoundException
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.helpers.getHistogramTimer
import uk.gov.gdx.datashare.models.DeathNotificationDetails
import uk.gov.gdx.datashare.models.EventNotification
import uk.gov.gdx.datashare.repositories.AcquirerEvent
import uk.gov.gdx.datashare.repositories.AcquirerEventRepository
import uk.gov.gdx.datashare.repositories.AcquirerSubscription
import uk.gov.gdx.datashare.repositories.AcquirerSubscriptionRepository
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

class AcquirerEventServiceTest {
  private val authenticationFacade = mockk<AuthenticationFacade>()
  private val acquirerSubscriptionRepository = mockk<AcquirerSubscriptionRepository>()
  private val acquirerEventRepository = mockk<AcquirerEventRepository>()
  private val deathNotificationService = mockk<DeathNotificationService>()
  private val dateTimeHandler = mockk<DateTimeHandler>()
  private val meterRegistry = mockk<MeterRegistry>()
  private val dataCreationToDeletionTimer = mockk<Timer>()
  private val eventDeletedCounter = mockk<Counter>()
  private val acquirersService = mockk<AcquirersService>()

  private val underTest: AcquirerEventService

  init {
    mockkStatic(::getHistogramTimer)
    every { getHistogramTimer(meterRegistry, "DATA_PROCESSING.TimeFromCreationToDeletion") }.returns(
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
    underTest = AcquirerEventService(
      authenticationFacade,
      acquirerSubscriptionRepository,
      acquirerEventRepository,
      dateTimeHandler,
      meterRegistry,
      listOf(
        deathNotificationService,
        TestEventEnrichmentService(),
      ),
      acquirersService,
    )
  }

  @BeforeEach
  fun setup() {
    every { authenticationFacade.getUsername() }.returns(clientId)
  }

  @Test
  fun `getEvent gets Event for client`() {
    val event = deathEvents.first()
    val deathNotificationDetails = DeathNotificationDetails(
      listOf(
        EnrichmentField.SOURCE_ID,
        EnrichmentField.FIRST_NAMES,
        EnrichmentField.LAST_NAME,
        EnrichmentField.ADDRESS,
      ),
      firstNames = "Alice",
      lastName = "Smith",
      address = deathNotificationSubscription.id.toString(),
    )

    every { acquirerEventRepository.findByClientIdAndId(clientId, event.id) }.returns(event)
    every { acquirerSubscriptionRepository.findByEventId(event.id) }.returns(deathNotificationSubscription)
    every {
      deathNotificationService.process(
        event.dataId,
        subscriptionEnrichmentFields,
      )
    }.returns(deathNotificationDetails)
    every { deathNotificationService.accepts(eventType = EventType.DEATH_NOTIFICATION) }.returns(true)
    every { acquirersService.getEnrichmentFieldsForAcquirerSubscription(any()) }.returns(subscriptionEnrichmentFields)

    val eventOutput = underTest.getEvent(event.id)

    assertThat(eventOutput).isEqualTo(
      EventNotification(
        eventId = event.id,
        eventType = EventType.DEATH_NOTIFICATION,
        sourceId = event.dataId,
        enrichmentFields = null,
        eventData = deathNotificationDetails,
      ),
    )
  }

  @Test
  fun `getEvent for event that does not exist for client, throws`() {
    val event = deathEvents.first()

    every { acquirerEventRepository.findByClientIdAndId(clientId, event.id) }.returns(null)

    val exception = assertThrows<EventNotFoundException> { underTest.getEvent(event.id) }

    assertThat(exception.message).isEqualTo("Event ${event.id} not found for polling client $clientId")
  }

  @Test
  fun `getEvent for subscription that does not exist for client, throws`() {
    val event = deathEvents.first()

    every { acquirerEventRepository.findByClientIdAndId(clientId, event.id) }.returns(event)
    every { acquirerSubscriptionRepository.findByEventId(event.id) }.returns(null)

    val exception = assertThrows<AcquirerSubscriptionNotFoundException> { underTest.getEvent(event.id) }

    assertThat(exception.message).isEqualTo("Acquirer subscription not found for event ${event.id}")
  }

  @Test
  fun `getEvents gets Events for client`() {
    val eventTypes = listOf(EventType.DEATH_NOTIFICATION)
    val startTime = LocalDateTime.now().minusHours(1)
    val endTime = LocalDateTime.now().plusHours(1)
    val deathNotificationDetails = DeathNotificationDetails(
      listOf(
        EnrichmentField.SOURCE_ID,
        EnrichmentField.FIRST_NAMES,
        EnrichmentField.LAST_NAME,
        EnrichmentField.ADDRESS,
      ),
      firstNames = "Alice",
      lastName = "Smith",
      address = deathNotificationSubscription.id.toString(),
    )

    every {
      acquirerSubscriptionRepository.findAllByOauthClientIdAndWhenDeletedIsNullAndEventTypeIsIn(
        clientId,
        eventTypes,
      )
    }
      .returns(listOf(deathNotificationSubscription))
    every {
      acquirerEventRepository.findPageByAcquirerSubscriptions(
        listOf(deathNotificationSubscription.id),
        startTime,
        endTime,
        10,
        0,
      )
    }.returns(deathEvents)
    every {
      acquirerEventRepository.countByAcquirerSubscriptions(
        listOf(deathNotificationSubscription.id),
        startTime,
        endTime,
      )
    }.returns(deathEvents.count())
    every {
      deathNotificationService.process(
        any(),
        subscriptionEnrichmentFields,
      )
    }.returns(deathNotificationDetails)
    every { deathNotificationService.accepts(eventType = EventType.DEATH_NOTIFICATION) }.returns(true)
    every { acquirersService.getEnrichmentFieldsForAcquirerSubscription(any()) }.returns(subscriptionEnrichmentFields)

    val eventsOutput = underTest.getEvents(eventTypes, startTime, endTime, 0, 10)

    assertThat(eventsOutput.eventModels).isEqualTo(
      deathEvents.map {
        EventNotification(
          eventId = it.id,
          eventType = EventType.DEATH_NOTIFICATION,
          sourceId = it.dataId,
          dataIncluded = true,
          enrichmentFields = subscriptionEnrichmentFields,
          eventData = deathNotificationDetails,
        )
      }.toList(),
    )
    assertThat(eventsOutput.count).isEqualTo(deathEvents.count())
  }

  @Test
  fun `getEvents returns the full count when paginated`() {
    val eventTypes = listOf(EventType.DEATH_NOTIFICATION)
    val startTime = LocalDateTime.now().minusHours(1)
    val endTime = LocalDateTime.now().plusHours(1)
    val deathNotificationDetails = DeathNotificationDetails(
      listOf(
        EnrichmentField.SOURCE_ID,
        EnrichmentField.FIRST_NAMES,
        EnrichmentField.LAST_NAME,
        EnrichmentField.ADDRESS,
      ),
      firstNames = "Alice",
      lastName = "Smith",
      address = deathNotificationSubscription.id.toString(),
    )
    val totalEventCount = 156

    every {
      acquirerSubscriptionRepository.findAllByOauthClientIdAndWhenDeletedIsNullAndEventTypeIsIn(
        clientId,
        eventTypes,
      )
    }
      .returns(listOf(deathNotificationSubscription))
    every {
      acquirerEventRepository.findPageByAcquirerSubscriptions(
        listOf(deathNotificationSubscription.id),
        startTime,
        endTime,
        10,
        0,
      )
    }.returns(deathEvents)
    every {
      acquirerEventRepository.countByAcquirerSubscriptions(
        listOf(deathNotificationSubscription.id),
        startTime,
        endTime,
      )
    }.returns(totalEventCount)
    every {
      deathNotificationService.process(
        any(),
        subscriptionEnrichmentFields,
      )
    }.returns(deathNotificationDetails)
    every { deathNotificationService.accepts(eventType = EventType.DEATH_NOTIFICATION) }.returns(true)
    every { acquirersService.getEnrichmentFieldsForAcquirerSubscription(any()) }.returns(subscriptionEnrichmentFields)

    val eventsOutput = underTest.getEvents(eventTypes, startTime, endTime, 0, 10)

    assertThat(eventsOutput.eventModels).isEqualTo(
      deathEvents.map {
        EventNotification(
          eventId = it.id,
          eventType = EventType.DEATH_NOTIFICATION,
          sourceId = it.dataId,
          dataIncluded = true,
          enrichmentFields = subscriptionEnrichmentFields,
          eventData = deathNotificationDetails,
        )
      }.toList(),
    )
    assertThat(eventsOutput.count).isEqualTo(totalEventCount)
  }

  @Test
  fun `getEvents gets thin events for client`() {
    val eventTypes = listOf(EventType.DEATH_NOTIFICATION)
    val startTime = LocalDateTime.now().minusHours(1)
    val endTime = LocalDateTime.now().plusHours(1)

    every {
      acquirerSubscriptionRepository.findAllByOauthClientIdAndWhenDeletedIsNullAndEventTypeIsIn(
        clientId,
        eventTypes,
      )
    }
      .returns(listOf(thinDeathNotificationSubscription))
    every {
      acquirerEventRepository.findPageByAcquirerSubscriptions(
        listOf(thinDeathNotificationSubscription.id),
        startTime,
        endTime,
        10,
        0,
      )
    }.returns(thinDeathEvents)
    every {
      acquirerEventRepository.countByAcquirerSubscriptions(
        listOf(thinDeathNotificationSubscription.id),
        startTime,
        endTime,
      )
    }.returns(thinDeathEvents.count())
    every { acquirersService.getEnrichmentFieldsForAcquirerSubscription(any()) }.returns(subscriptionEnrichmentFields)

    val eventsOutput = underTest.getEvents(eventTypes, startTime, endTime, 0, 10)

    assertThat(eventsOutput.eventModels).isEqualTo(
      thinDeathEvents.map {
        EventNotification(
          eventId = it.id,
          eventType = EventType.DEATH_NOTIFICATION,
          sourceId = it.dataId,
          dataIncluded = false,
          enrichmentFields = subscriptionEnrichmentFields,
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
      listOf(
        EnrichmentField.SOURCE_ID,
        EnrichmentField.FIRST_NAMES,
        EnrichmentField.LAST_NAME,
        EnrichmentField.ADDRESS,
      ),
      firstNames = "Bob",
      lastName = "Smith",
      address = deathNotificationSubscription.id.toString(),
    )

    every { acquirerSubscriptionRepository.findAllByOauthClientIdAndWhenDeletedIsNull(clientId) }
      .returns(listOf(deathNotificationSubscription))
    every {
      acquirerEventRepository.findPageByAcquirerSubscriptions(
        listOf(deathNotificationSubscription.id),
        fallbackStartTime,
        fallbackEndTime,
        10,
        0,
      )
    }.returns(extraDeathEvents)
    every {
      acquirerEventRepository.countByAcquirerSubscriptions(
        listOf(deathNotificationSubscription.id),
        fallbackStartTime,
        fallbackEndTime,
      )
    }.returns(extraDeathEvents.count())
    every {
      deathNotificationService.process(
        any(),
        subscriptionEnrichmentFields,
      )
    }.returns(deathNotificationDetails)
    every { deathNotificationService.accepts(eventType = EventType.DEATH_NOTIFICATION) }.returns(true)
    every { acquirersService.getEnrichmentFieldsForAcquirerSubscription(any()) }.returns(subscriptionEnrichmentFields)

    val eventStatusOutput = underTest.getEvents(null, null, null, 0, 10)

    assertThat(eventStatusOutput.eventModels).isEqualTo(
      extraDeathEvents.map {
        EventNotification(
          eventId = it.id,
          eventType = EventType.DEATH_NOTIFICATION,
          sourceId = it.dataId,
          dataIncluded = true,
          enrichmentFields = subscriptionEnrichmentFields,
          eventData = deathNotificationDetails,
        )
      }.toList(),
    )
    assertThat(eventStatusOutput.count).isEqualTo(extraDeathEvents.count())
  }

  @Test
  fun `deleteEvent deletes event`() {
    val event = AcquirerEvent(
      acquirerSubscriptionId = UUID.randomUUID(),
      dataId = "HMPO",
      supplierEventId = UUID.randomUUID(),
      eventTime = null,
    )
    val now = LocalDateTime.now()
    every { acquirerEventRepository.findByClientIdAndId(clientId, event.id) }.returns(event)
    every { acquirerSubscriptionRepository.findByEventId(event.id) }.returns(
      deathNotificationSubscription,
    )
    every { dateTimeHandler.now() }.returns(now)
    every { acquirersService.getEnrichmentFieldsForAcquirerSubscription(any()) }.returns(subscriptionEnrichmentFields)

    every { acquirerEventRepository.softDeleteById(event.id, now) }.returns(Unit)

    underTest.deleteEvent(event.id)

    verify(exactly = 1) { acquirerEventRepository.softDeleteById(event.id, now) }
    verify(exactly = 1) { eventDeletedCounter.increment() }
  }

  @Test
  fun `deleteEvent throws if event not found for client`() {
    val eventId = UUID.randomUUID()
    every { acquirerEventRepository.findByClientIdAndId(clientId, eventId) }.returns(null)

    val exception = assertThrows<EventNotFoundException> {
      underTest.deleteEvent(eventId)
    }

    assertThat(exception.message).isEqualTo("Event $eventId not found for callback client $clientId")

    verify(exactly = 0) { acquirerEventRepository.softDeleteById(any(), any()) }
    verify(exactly = 0) { eventDeletedCounter.increment() }
  }

  private val clientId = "ClientId"
  private val deathNotificationSubscription = AcquirerSubscription(
    acquirerId = UUID.randomUUID(),
    oauthClientId = clientId,
    eventType = EventType.DEATH_NOTIFICATION,
    enrichmentFieldsIncludedInPoll = true,
  )
  private val thinDeathNotificationSubscription = AcquirerSubscription(
    acquirerId = UUID.randomUUID(),
    oauthClientId = clientId,
    eventType = EventType.DEATH_NOTIFICATION,
    enrichmentFieldsIncludedInPoll = false,
  )
  private val subscriptionEnrichmentFields =
    listOf(EnrichmentField.SOURCE_ID, EnrichmentField.FIRST_NAMES, EnrichmentField.LAST_NAME)
  private val deathEvents = getEvents(4, deathNotificationSubscription.id)
  private val thinDeathEvents = getEvents(4, thinDeathNotificationSubscription.id)
  private val extraDeathEvents = getEvents(10, deathNotificationSubscription.id)

  private fun getEvents(
    count: Int,
    subscriptionId: UUID = UUID.randomUUID(),
  ): List<AcquirerEvent> =
    List(count) {
      AcquirerEvent(
        acquirerSubscriptionId = subscriptionId,
        dataId = "HMPO",
        supplierEventId = UUID.randomUUID(),
        eventTime = null,
      )
    }
}
