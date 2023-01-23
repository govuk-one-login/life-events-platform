package uk.gov.gdx.datashare.service

import com.fasterxml.jackson.annotation.JsonInclude
import io.micrometer.core.instrument.MeterRegistry
import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.gdx.datashare.config.AuthenticationFacade
import uk.gov.gdx.datashare.config.ConsumerSubscriptionNotFoundException
import uk.gov.gdx.datashare.config.DateTimeHandler
import uk.gov.gdx.datashare.config.EventNotFoundException
import uk.gov.gdx.datashare.repository.ConsumerSubscriptionRepository
import uk.gov.gdx.datashare.repository.EventDataRepository
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class EventDataService(
  private val authenticationFacade: AuthenticationFacade,
  private val consumerSubscriptionRepository: ConsumerSubscriptionRepository,
  private val eventDataRepository: EventDataRepository,
  private val deathNotificationService: DeathNotificationService,
  private val dateTimeHandler: DateTimeHandler,
  private val meterRegistry: MeterRegistry,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun getEventsStatus(
    optionalStartTime: LocalDateTime?,
    optionalEndTime: LocalDateTime?,
  ): Flow<EventStatus> {
    val startTime = optionalStartTime ?: dateTimeHandler.defaultStartTime()
    val endTime = optionalEndTime ?: dateTimeHandler.now()
    val clientId = authenticationFacade.getUsername()

    val consumerSubscriptions = consumerSubscriptionRepository.findAllByClientId(clientId)

    return consumerSubscriptions.map {
      EventStatus(
        eventType = it.eventType,
        count = eventDataRepository.findAllByConsumerSubscription(it.id, startTime, endTime).count(),
      )
    }
  }

  suspend fun getEvent(
    id: UUID,
  ): EventNotification? {
    val clientId = authenticationFacade.getUsername()
    val event = eventDataRepository.findByClientIdAndId(clientId, id)
      ?: throw EventNotFoundException("Event $id not found for polling client $clientId")
    val consumerSubscription = consumerSubscriptionRepository.findByEventId(id)
      ?: throw ConsumerSubscriptionNotFoundException("Consumer subscription not found for event $id")

    return event.let {
      EventNotification(
        eventId = it.id,
        eventType = consumerSubscription.eventType,
        sourceId = it.dataId,
        eventData = it.dataPayload?.let { dataPayload ->
          deathNotificationService.mapDeathNotification(dataPayload)
        },
      )
    }
  }

  suspend fun getEvents(
    eventTypes: List<String>?,
    optionalStartTime: LocalDateTime?,
    optionalEndTime: LocalDateTime?,
  ): Flow<EventNotification> {
    val startTime = optionalStartTime ?: dateTimeHandler.defaultStartTime()
    val endTime = optionalEndTime ?: dateTimeHandler.now()
    val clientId = authenticationFacade.getUsername()

    val consumerSubscriptions = eventTypes?.let {
      consumerSubscriptionRepository.findAllByEventTypesAndClientId(clientId, eventTypes)
    } ?: consumerSubscriptionRepository.findAllByClientId(clientId)

    if (consumerSubscriptions.count() == 0) {
      return emptyFlow()
    }

    val consumerSubscriptionIdMap = consumerSubscriptions.toList().associateBy({ it.id }, { it })

    val events = eventDataRepository.findAllByConsumerSubscriptions(
      consumerSubscriptionIdMap.keys.toList(),
      startTime,
      endTime,
    )

    return events.map { event ->
      val subscription = consumerSubscriptionIdMap[event.consumerSubscriptionId]!!
      EventNotification(
        eventId = event.id,
        eventType = subscription.eventType,
        sourceId = event.dataId,
        eventData = if (subscription.enrichmentFieldsIncludedInPoll) {
          event.dataPayload?.let { dataPayload ->
            deathNotificationService.mapDeathNotification(dataPayload)
          }
        } else {
          null
        },
      )
    }
  }

  suspend fun deleteEvent(id: UUID) {
    val callbackClientId = authenticationFacade.getUsername()
    val event = eventDataRepository.findByClientIdAndId(callbackClientId, id)
      ?: throw EventNotFoundException("Event $id not found for callback client $callbackClientId")
    val consumerSubscription = consumerSubscriptionRepository.findByEventId(id)
      ?: throw ConsumerSubscriptionNotFoundException("Consumer subscription not found for event $id")

    eventDataRepository.delete(event)
    meterRegistry.counter(
      "EVENT_ACTION.EventDeleted",
      "eventType",
      consumerSubscription.eventType,
      "consumerSubscription",
      event.consumerSubscriptionId.toString(),
    ).increment()
    if (event.whenCreated != null) {
      meterRegistry.timer("DATA_PROCESSING.TimeFromCreationToDeletion")
        .record(Duration.between(event.whenCreated, dateTimeHandler.now()).abs())
    }
  }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Subscribed event notification")
data class EventNotification(
  @Schema(description = "Event ID (UUID)", required = true, example = "d8a6f3ba-e915-4e79-8479-f5f5830f4622")
  val eventId: UUID,
  @Schema(
    description = "Event's Type",
    required = true,
    example = "DEATH_NOTIFICATION",
    allowableValues = ["DEATH_NOTIFICATION", "LIFE_EVENT"],
  )
  val eventType: String,
  @Schema(description = "ID from the source of the notification", required = true, example = "999999901")
  val sourceId: String,
  @Schema(
    description = "Event Data - only returned when the consumer has `enrichmentFieldsIncludedInPoll` enabled, otherwise an empty object. " +
      "Full dataset for the event can be obtained by calling /events/{id}",
    required = false,
  )
  val eventData: Any?,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Event type count")
data class EventStatus(
  @Schema(
    description = "Event's Type",
    required = true,
    example = "DEATH_NOTIFICATION",
    allowableValues = ["DEATH_NOTIFICATION", "LIFE_EVENT"],
  )
  val eventType: String,
  @Schema(description = "Number of events for the type", required = true, example = "123")
  val count: Number,
)
