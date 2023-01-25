package uk.gov.gdx.datashare.service

import com.fasterxml.jackson.annotation.JsonInclude
import com.toedter.spring.hateoas.jsonapi.JsonApiId
import com.toedter.spring.hateoas.jsonapi.JsonApiTypeForClass
import io.micrometer.core.instrument.MeterRegistry
import io.swagger.v3.oas.annotations.media.Schema
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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

  fun getEventsStatus(
    optionalStartTime: LocalDateTime?,
    optionalEndTime: LocalDateTime?,
  ): List<EventStatus> {
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

  fun getEvent(
    id: UUID,
  ): EventNotification {
    val clientId = authenticationFacade.getUsername()
    val event = eventDataRepository.findByClientIdAndId(clientId, id)
      ?: throw EventNotFoundException("Event $id not found for polling client $clientId")
    val consumerSubscription = consumerSubscriptionRepository.findByEventId(id)
      ?: throw ConsumerSubscriptionNotFoundException("Consumer subscription not found for event $id")

    return mapEventNotification(event, consumerSubscription, includeData = true, callbackEvent = true)
  }

  fun getEvents(
    eventTypes: List<String>?,
    optionalStartTime: LocalDateTime?,
    optionalEndTime: LocalDateTime?,
    pageNumber: Int,
    pageSize: Int,
  ): Events {
    val startTime = optionalStartTime ?: dateTimeHandler.defaultStartTime()
    val endTime = optionalEndTime ?: dateTimeHandler.now()
    val clientId = authenticationFacade.getUsername()

    val consumerSubscriptions = eventTypes?.let {
      consumerSubscriptionRepository.findAllByEventTypesAndClientId(clientId, eventTypes)
    } ?: consumerSubscriptionRepository.findAllByClientId(clientId)

    if (consumerSubscriptions.isEmpty()) {
      return Events(0, emptyList())
    }

    val consumerSubscriptionIdMap = consumerSubscriptions.toList().associateBy({ it.id }, { it })

    val events = eventDataRepository.findPageByConsumerSubscriptions(
      consumerSubscriptionIdMap.keys.toList(),
      startTime,
      endTime,
      pageSize,
      (pageSize * pageNumber),
    )

    val eventModels = events.map { event ->
      val subscription = consumerSubscriptionIdMap[event.consumerSubscriptionId]!!
      mapEventNotification(event, subscription, subscription.enrichmentFieldsIncludedInPoll)
    }

    val eventsCount = eventDataRepository.countByConsumerSubscriptions(
      consumerSubscriptionIdMap.keys.toList(),
      startTime,
      endTime,
    )

    return Events(eventsCount, eventModels)
  }

  fun deleteEvent(id: UUID): EventNotification {
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
    meterRegistry.timer("DATA_PROCESSING.TimeFromCreationToDeletion")
      .record(Duration.between(event.whenCreated, dateTimeHandler.now()).abs())
    return mapEventNotification(event, consumerSubscription, false)
  }

  private fun mapEventNotification(
    event: EventData,
    subscription: ConsumerSubscription,
    includeData: Boolean = false,
    callbackEvent: Boolean = false,
  ): EventNotification {
    return EventNotification(
      eventId = event.id,
      eventType = subscription.eventType,
      sourceId = event.dataId,
      dataIncluded = if (!callbackEvent) includeData else null,
      enrichmentFields = if (!callbackEvent) subscription.enrichmentFields else null,
      eventData = if (includeData) {
        event.dataPayload?.let { dataPayload ->
          deathNotificationService.mapDeathNotification(dataPayload)
        }
      } else {
        null
      },
    )
  }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Subscribed event notification")
@JsonApiTypeForClass("events")
data class EventNotification(
  @Schema(description = "Event ID (UUID)", required = true, example = "d8a6f3ba-e915-4e79-8479-f5f5830f4622")
  @JsonApiId
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
    description = "Indicates that event data is returned when true,",
    required = false,
    example = "false",
  )
  val dataIncluded: Boolean? = null,
  @Schema(
    description = "list of data fields that will be returned in this event",
    required = false,
    example = "firstNames,lastName,sex,dateOfDeath",
  )
  val enrichmentFields: String? = null,
  @Schema(
    description = "<h2>Event Data</h2>" +
      "     This field is only populated when the consumer has <em>enrichmentFieldsIncludedInPoll</em> enabled, otherwise an empty object." +
      "     Full dataset for the event can be obtained by calling <pre>/events/{id}</pre>" +
      "     <h3>Event Types</h3>" +
      "     <h4>1. Death Notification - Type: <em>DEATH_NOTIFICATION</em></h4>" +
      "     <p>Death notifications take the following json structure." +
      "     <pre>\n" +
      "{\n" +
      "        \"registrationDate\": \"2023-01-23\",\n" +
      "        \"firstNames\": \"Mary Jane\",\n" +
      "        \"lastName\": \"Smith\",\n" +
      "        \"maidenName\": \"Jones\",\n" +
      "        \"sex\": \"Male\",\n" +
      "        \"dateOfDeath\": \"2023-01-02\",\n" +
      "        \"dateOfBirth\": \"1972-02-20\",\n" +
      "        \"birthPlace\": \"56 Test Address, B Town\",\n" +
      "        \"deathPlace\": \"Hospital Ward 5, C Town\",\n" +
      "        \"occupation\": \"Doctor\",\n" +
      "        \"retired\": true,\n" +
      "        \"address\": \"101 Address Street, A Town, Postcode\"\n" +
      "}\n" +
      "      </pre>" +
      "      <p><b>Mandatory Fields</b>: registrationDate, firstNames, lastName, sex, dateOfDeath</p>",
    required = false,
  )
  val eventData: Any? = null,
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

data class Events(val count: Int, val eventModels: List<EventNotification>)
