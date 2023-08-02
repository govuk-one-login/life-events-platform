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
import uk.gov.gdx.datashare.models.EventDetails
import uk.gov.gdx.datashare.models.EventNotification
import uk.gov.gdx.datashare.models.Events
import uk.gov.gdx.datashare.repositories.AcquirerEvent
import uk.gov.gdx.datashare.repositories.AcquirerEventRepository
import uk.gov.gdx.datashare.repositories.AcquirerSubscription
import uk.gov.gdx.datashare.repositories.AcquirerSubscriptionRepository
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
@XRayEnabled
class AcquirerEventService(
  private val authenticationFacade: AuthenticationFacade,
  private val acquirerSubscriptionRepository: AcquirerSubscriptionRepository,
  private val acquirerEventRepository: AcquirerEventRepository,
  private val dateTimeHandler: DateTimeHandler,
  private val meterRegistry: MeterRegistry,
  private val enrichmentServices: List<EnrichmentService>,
  private val acquirersService: AcquirersService,
) {
  private val dataProcessingTimer = getHistogramTimer(meterRegistry, "DATA_PROCESSING.TimeFromCreationToDeletion")

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getEvent(
    id: UUID,
  ): EventNotification {
    val clientId = authenticationFacade.getUsername()
    val event = acquirerEventRepository.findByClientIdAndId(clientId, id)
      ?: throw EventNotFoundException("Event $id not found for polling client $clientId")
    val acquirerSubscription = acquirerSubscriptionRepository.findByEventId(id)
      ?: throw AcquirerSubscriptionNotFoundException("Acquirer subscription not found for event $id")

    return buildEnrichedEventNotification(event, acquirerSubscription)
  }

  fun buildEnrichedEventNotification(
    event: AcquirerEvent,
    acquirerSubscription: AcquirerSubscription,
  ): EventNotification {
    val enrichmentFieldNames = acquirersService.getEnrichmentFieldsForAcquirerSubscription(acquirerSubscription)

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
      acquirerSubscriptionRepository.findAllByOauthClientIdAndWhenDeletedIsNullAndEventTypeIsIn(clientId, eventTypes)
    } ?: acquirerSubscriptionRepository.findAllByOauthClientIdAndWhenDeletedIsNull(clientId)

    if (acquirerSubscriptions.isEmpty()) {
      return Events(0, emptyList())
    }

    val acquirerSubscriptionIdMap = acquirerSubscriptions.toList().associateBy({ it.id }, { it })
    val acquirerSubscriptionEnrichmentFieldsById = acquirerSubscriptions.toList().associateBy(
      { it.id },
      { acquirersService.getEnrichmentFieldsForAcquirerSubscription(it) },
    )

    val events = acquirerEventRepository.findPageByAcquirerSubscriptions(
      acquirerSubscriptionIdMap.keys.toList(),
      startTime,
      endTime,
      pageSize,
      (pageSize * pageNumber),
    )

    val eventModels = events.map { event ->
      val subscription = acquirerSubscriptionIdMap.getOrElse(event.acquirerSubscriptionId) {
        throw NoSuchElementException("Acquirer subscription ID does not exist in acquirer subscription map")
      }
      val enrichmentFieldNames = acquirerSubscriptionEnrichmentFieldsById.getOrElse(event.acquirerSubscriptionId) {
        throw NoSuchElementException("Acquirer subscription ID does not exist in enrichment field map")
      }
      mapEventNotification(event, subscription, enrichmentFieldNames, subscription.enrichmentFieldsIncludedInPoll)
    }

    val eventsCount = acquirerEventRepository.countByAcquirerSubscriptions(
      acquirerSubscriptionIdMap.keys.toList(),
      startTime,
      endTime,
    )

    return Events(eventsCount, eventModels)
  }

  fun deleteEvent(id: UUID): EventNotification {
    val callbackClientId = authenticationFacade.getUsername()
    val event = acquirerEventRepository.findByClientIdAndId(callbackClientId, id)
      ?: throw EventNotFoundException("Event $id not found for callback client $callbackClientId")
    val acquirerSubscription = acquirerSubscriptionRepository.findByEventId(id)
      ?: throw AcquirerSubscriptionNotFoundException("Acquirer subscription not found for event $id")

    acquirerEventRepository.softDeleteById(event.id, dateTimeHandler.now())
    meterRegistry.counter(
      "EVENT_ACTION.EventDeleted",
      "eventType",
      acquirerSubscription.eventType.name,
      "acquirerSubscription",
      event.acquirerSubscriptionId.toString(),
    ).increment()
    dataProcessingTimer.record(Duration.between(event.createdAt, dateTimeHandler.now()).abs())
    return mapEventNotification(event, acquirerSubscription, emptyList(), false)
  }

  private fun mapEventNotification(
    event: AcquirerEvent,
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
      eventData = if (includeData) {
        callbackAndEnrichData(
          subscription.eventType,
          event.dataId,
          enrichmentFieldNames,
        )
      } else {
        null
      },
    )
  }

  private fun callbackAndEnrichData(
    eventType: EventType,
    dataId: String,
    enrichmentFieldNames: List<EnrichmentField>,
  ): EventDetails? {
    return enrichmentServices.single { p -> p.accepts(eventType) }
      .process(dataId, enrichmentFieldNames)
  }
}
