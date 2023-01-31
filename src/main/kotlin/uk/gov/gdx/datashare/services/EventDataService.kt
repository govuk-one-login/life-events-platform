package uk.gov.gdx.datashare.services

import com.amazonaws.xray.spring.aop.XRayEnabled
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.gdx.datashare.config.AcquirerSubscriptionNotFoundException
import uk.gov.gdx.datashare.config.AuthenticationFacade
import uk.gov.gdx.datashare.config.DateTimeHandler
import uk.gov.gdx.datashare.config.EventNotFoundException
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.models.EventNotification
import uk.gov.gdx.datashare.models.Events
import uk.gov.gdx.datashare.repositories.*
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
@XRayEnabled
class EventDataService(
  private val authenticationFacade: AuthenticationFacade,
  private val acquirerSubscriptionRepository: AcquirerSubscriptionRepository,
  private val eventDataRepository: EventDataRepository,
  private val deathRegistrationLookupService: DeathRegistrationLookupService,
  private val prisonerLookupService: PrisonerLookupService,
  private val dateTimeHandler: DateTimeHandler,
  private val meterRegistry: MeterRegistry,
  private val acquirerSubscriptionEnrichmentFieldRepository: AcquirerSubscriptionEnrichmentFieldRepository,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getEvent(
    id: UUID,
  ): EventNotification {
    val clientId = authenticationFacade.getUsername()
    val event = eventDataRepository.findByClientIdAndId(clientId, id)
      ?: throw EventNotFoundException("Event $id not found for polling client $clientId")
    val acquirerSubscription = acquirerSubscriptionRepository.findByEventId(id)
      ?: throw AcquirerSubscriptionNotFoundException("Acquirer subscription not found for event $id")

    return mapEventNotification(event, acquirerSubscription, includeData = true, callbackEvent = true)
  }

  fun getEvents(
    eventTypes: List<EventType>?,
    optionalStartTime: LocalDateTime?,
    optionalEndTime: LocalDateTime?,
    pageNumber: Int,
    pageSize: Int,
  ): Events {
    val startTime = optionalStartTime ?: dateTimeHandler.defaultStartTime()
    val endTime = optionalEndTime ?: dateTimeHandler.now()
    val clientId = authenticationFacade.getUsername()

    val acquirerSubscriptions = eventTypes?.let {
      acquirerSubscriptionRepository.findAllByOauthClientIdAndEventTypeIsIn(clientId, eventTypes)
    } ?: acquirerSubscriptionRepository.findAllByOauthClientId(clientId)

    if (acquirerSubscriptions.isEmpty()) {
      return Events(0, emptyList())
    }

    val acquirerSubscriptionIdMap = acquirerSubscriptions.toList().associateBy({ it.id }, { it })

    val events = eventDataRepository.findPageByAcquirerSubscriptions(
      acquirerSubscriptionIdMap.keys.toList(),
      startTime,
      endTime,
      pageSize,
      (pageSize * pageNumber),
    )

    val eventModels = events.map { event ->
      val subscription = acquirerSubscriptionIdMap[event.acquirerSubscriptionId]!!
      mapEventNotification(event, subscription, subscription.enrichmentFieldsIncludedInPoll)
    }

    val eventsCount = eventDataRepository.countByAcquirerSubscriptions(
      acquirerSubscriptionIdMap.keys.toList(),
      startTime,
      endTime,
    )

    return Events(eventsCount, eventModels)
  }

  fun deleteEvent(id: UUID): EventNotification {
    val callbackClientId = authenticationFacade.getUsername()
    val event = eventDataRepository.findByClientIdAndId(callbackClientId, id)
      ?: throw EventNotFoundException("Event $id not found for callback client $callbackClientId")
    val acquirerSubscription = acquirerSubscriptionRepository.findByEventId(id)
      ?: throw AcquirerSubscriptionNotFoundException("Acquirer subscription not found for event $id")

    eventDataRepository.softDeleteById(event.id, dateTimeHandler.now())
    meterRegistry.counter(
      "EVENT_ACTION.EventDeleted",
      "eventType",
      acquirerSubscription.eventType.name,
      "acquirerSubscription",
      event.acquirerSubscriptionId.toString(),
    ).increment()
    meterRegistry.timer("DATA_PROCESSING.TimeFromCreationToDeletion")
      .record(Duration.between(event.whenCreated, dateTimeHandler.now()).abs())
    return mapEventNotification(event, acquirerSubscription, false)
  }

  private fun mapEventNotification(
    event: EventData,
    subscription: AcquirerSubscription,
    includeData: Boolean = false,
    callbackEvent: Boolean = false,
  ): EventNotification {
    val enrichmentFields =
      acquirerSubscriptionEnrichmentFieldRepository.findAllByAcquirerSubscriptionId(subscription.acquirerSubscriptionId)
        .map { it.enrichmentField }
    return EventNotification(
      eventId = event.id,
      eventType = subscription.eventType,
      sourceId = event.dataId,
      dataIncluded = if (!callbackEvent) includeData else null,
      enrichmentFields = if (!callbackEvent) enrichmentFields else null,
      eventData = if (includeData) callbackAndEnrichData(subscription, event, enrichmentFields) else null,
    )
  }

  @Suppress("IMPLICIT_CAST_TO_ANY")
  private fun callbackAndEnrichData(
    subscription: ConsumerSubscription,
    event: EventData,
    enrichmentFields: List<String>,
  ) = when (subscription.eventType) {
    EventType.DEATH_NOTIFICATION -> deathRegistrationLookupService.getEnrichedPayload(event.dataId, enrichmentFields)
    EventType.ENTERED_PRISON -> prisonerLookupService.getEnrichedPayload(event.dataId, enrichmentFields)
    else -> log.warn("Not handling this event type {}", subscription.eventType)
  }
}
