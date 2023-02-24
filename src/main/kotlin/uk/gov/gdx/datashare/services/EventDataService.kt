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
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.helpers.getHistogramTimer
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
  private val dateTimeHandler: DateTimeHandler,
  private val meterRegistry: MeterRegistry,
  private val acquirerSubscriptionEnrichmentFieldRepository: AcquirerSubscriptionEnrichmentFieldRepository,
  private val enrichmentServices: List<EnrichmentService>,
) {
  private val dataProcessingTimer = getHistogramTimer(meterRegistry, "DATA_PROCESSING.TimeFromCreationToDeletion")

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
    val enrichmentFieldNames = enrichmentFieldNamesForAcquirerSubscription(acquirerSubscription)

    return mapEventNotification(
      event,
      acquirerSubscription,
      enrichmentFieldNames,
      includeData = true,
      callbackEvent = true,
    )
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
    val acquirerSubscriptionEnrichmentFieldsById = acquirerSubscriptions.toList().associateBy(
      { it.id },
      { enrichmentFieldNamesForAcquirerSubscription(it) },
    )

    val events = eventDataRepository.findPageByAcquirerSubscriptions(
      acquirerSubscriptionIdMap.keys.toList(),
      startTime,
      endTime,
      pageSize,
      (pageSize * pageNumber),
    )

    val eventModels = events.map { event ->
      val subscription = acquirerSubscriptionIdMap[event.acquirerSubscriptionId]!!
      val enrichmentFieldNames = acquirerSubscriptionEnrichmentFieldsById[event.acquirerSubscriptionId]!!
      mapEventNotification(event, subscription, enrichmentFieldNames, subscription.enrichmentFieldsIncludedInPoll)
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
    dataProcessingTimer.record(Duration.between(event.whenCreated, dateTimeHandler.now()).abs())
    return mapEventNotification(event, acquirerSubscription, emptyList(), false)
  }

  private fun mapEventNotification(
    event: EventData,
    subscription: AcquirerSubscription,
    enrichmentFieldNames: List<EnrichmentField>,
    includeData: Boolean = false,
    callbackEvent: Boolean = false,
  ): EventNotification {
    return EventNotification(
      eventId = event.id,
      eventType = subscription.eventType,
      sourceId = if (EnrichmentField.SOURCE_ID in enrichmentFieldNames) event.dataId else null,
      dataIncluded = if (!callbackEvent) includeData else null,
      enrichmentFields = if (!callbackEvent) enrichmentFieldNames else null,
      eventData = if (includeData) callbackAndEnrichData(subscription, event, enrichmentFieldNames) else null,
    )
  }

  private fun callbackAndEnrichData(
    subscription: AcquirerSubscription,
    event: EventData,
    enrichmentFieldNames: List<EnrichmentField>,
  ): Any? {
    return enrichmentServices.single { p -> p.accepts(subscription.eventType) }
      .process(subscription.eventType, event.dataId, enrichmentFieldNames)
  }

  private fun enrichmentFieldNamesForAcquirerSubscription(it: AcquirerSubscription) =
    acquirerSubscriptionEnrichmentFieldRepository.findAllByAcquirerSubscriptionId(it.id).map { it.enrichmentField }
}
