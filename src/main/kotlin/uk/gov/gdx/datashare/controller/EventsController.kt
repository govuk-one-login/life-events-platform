package uk.gov.gdx.datashare.controller

import com.fasterxml.jackson.annotation.JsonInclude
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.links.Link
import io.swagger.v3.oas.annotations.links.LinkParameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import uk.gov.gdx.datashare.config.ErrorResponse
import uk.gov.gdx.datashare.config.JacksonConfiguration
import uk.gov.gdx.datashare.service.DataReceiverService
import uk.gov.gdx.datashare.service.EventApiAuditService
import uk.gov.gdx.datashare.service.EventDataService
import uk.gov.gdx.datashare.service.EventNotification
import uk.gov.gdx.datashare.service.EventStatus
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/events", produces = [MediaType.APPLICATION_JSON_VALUE])
@Validated
@Tag(name = "04. Events")
class EventsController(
  private val eventDataService: EventDataService,
  private val dataReceiverService: DataReceiverService,
  private val eventApiAuditService: EventApiAuditService,
  private val meterRegistry: MeterRegistry,
) {
  @PreAuthorize("hasAnyAuthority('SCOPE_events/consume')")
  @GetMapping("/status")
  @Operation(
    summary = "Event Get API - Get event status",
    description = "Get count of all events for consumer, Need scope of events/consume",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Count per type",
      ),
    ],
  )
  fun getEventsStatus(
    @DateTimeFormat(pattern = JacksonConfiguration.dateTimeFormat)
    @RequestParam(name = "fromTime", required = false)
    startTime: LocalDateTime? = null,
    @Schema(
      description = "Events before this time, if not supplied it will be now",
      type = "date-time",
      required = false,
    )
    @DateTimeFormat(pattern = JacksonConfiguration.dateTimeFormat)
    @RequestParam(name = "toTime", required = false)
    endTime: LocalDateTime? = null,
  ): List<EventStatus> = run {
    tryCallAndUpdateMetric(
      { eventDataService.getEventsStatus(startTime, endTime).toList() },
      meterRegistry.counter("API_CALLS.GetEventsStatus", "success", "true"),
      meterRegistry.counter("API_CALLS.GetEventsStatus", "success", "false"),
    )
  }

  @PreAuthorize("hasAnyAuthority('SCOPE_events/consume')")
  @GetMapping
  @Operation(
    operationId = "getEvents",
    summary = "Event Get API - Get event data",
    description = "Get all events for consumer, Need scope of events/consume. " +
      "Full data is only returned when the consumer has `enrichmentFieldsIncludedInPoll` enabled. " +
      "When this flag is not enabled, the eventData will be an empty object. " +
      "Full dataset for the event can be obtained by calling `/events/{id}`",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Events",
        links = [
          Link(
            name = "Get event details",
            operationId = "getEvent",
            parameters = [LinkParameter(name = "id", expression = "\$response.body#/0/eventId")],
            description = "Full details of the event",
          ),
        ],
      ),
    ],
  )
  fun getEvents(
    @Schema(
      description = "Event Types, if none supplied it will be the allowed types for this client",
      required = false,
      allowableValues = ["DEATH_NOTIFICATION", "LIFE_EVENT"],
    )
    @RequestParam(name = "eventType", required = false)
    eventTypes: List<String>?,
    @Schema(
      description = "Events after this time, if not supplied it will be from the last time this endpoint was called for this client",
      type = "date-time",
      required = false,
    )
    @DateTimeFormat(pattern = JacksonConfiguration.dateTimeFormat)
    @RequestParam(name = "fromTime", required = false)
    startTime: LocalDateTime? = null,
    @Schema(
      description = "Events before this time, if not supplied it will be now",
      type = "date-time",
      required = false,
    )
    @DateTimeFormat(pattern = JacksonConfiguration.dateTimeFormat)
    @RequestParam(name = "toTime", required = false)
    endTime: LocalDateTime? = null,
  ): List<EventNotification> = run {
    tryCallAndUpdateMetric(
      {
        val eventNotifications = eventDataService.getEvents(eventTypes, startTime, endTime).toList()
        eventApiAuditService.auditEventApiCall(eventNotifications)
        eventNotifications
      },
      meterRegistry.counter("API_CALLS.GetEvents", "success", "true"),
      meterRegistry.counter("API_CALLS.GetEvents", "success", "false"),
    )
  }

  @PreAuthorize("hasAnyAuthority('SCOPE_events/publish')")
  @PostMapping
  @Operation(
    summary = "Send events to GDS - The 'Source' of the event - this could be HMPO or DWP for example",
    description = "Scope is events/publish",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Data Accepted",
      ),
    ],
  )
  fun publishEvent(
    @Schema(
      description = "Event Payload",
      required = true,
      implementation = EventToPublish::class,
    )
    @RequestBody
    eventPayload: EventToPublish,
  ) = run {
    tryCallAndUpdateMetric(
      { dataReceiverService.sendToDataProcessor(eventPayload) },
      meterRegistry.counter("API_CALLS.PublishEvent", "success", "true"),
      meterRegistry.counter("API_CALLS.PublishEvent", "success", "false"),
    )
  }

  @PreAuthorize("hasAnyAuthority('SCOPE_events/consume')")
  @GetMapping("/{id}")
  @Operation(
    summary = "Get Specific Event API - Get event data",
    description = "The event ID is the UUID received off the queue, Need scope of events/consume",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Event",
      ),
      ApiResponse(
        responseCode = "404",
        description = "Event with UUID cannot be found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getEvent(
    @Schema(description = "Event ID", required = true)
    @PathVariable
    id: UUID,
  ) = run {
    tryCallAndUpdateMetric(
      {
        val eventNotification = eventDataService.getEvent(id)
        eventApiAuditService.auditEventApiCall(eventNotification)
        eventNotification
      },
      meterRegistry.counter("API_CALLS.GetEvent", "success", "true"),
      meterRegistry.counter("API_CALLS.GetEvent", "success", "false"),
    )
  }

  @PreAuthorize("hasAnyAuthority('SCOPE_events/consume')")
  @DeleteMapping("/{id}")
  @Operation(
    summary = "Event Delete API - Delete event data",
    description = "The event ID is the UUID received off the queue, Need scope of events/consume",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "Event deleted",
      ),
    ],
  )
  fun deleteEvent(
    @Schema(description = "Event ID", required = true)
    @PathVariable
    id: UUID,
  ): ResponseEntity<Void> {
    try {
      val eventNotification = eventDataService.deleteEvent(id)
      eventApiAuditService.auditEventApiCall(eventNotification)
      meterRegistry.counter("API_CALLS.DeleteEvent", "success", "true").increment()
      return ResponseEntity<Void>(HttpStatus.NO_CONTENT)
    } catch (e: Exception) {
      meterRegistry.counter("API_CALLS.DeleteEvent", "success", "false").increment()
      throw e
    }
  }

  private fun <T> tryCallAndUpdateMetric(
    call: () -> T,
    successCounter: Counter,
    failureCounter: Counter,
  ): T {
    try {
      val result = call()
      successCounter.increment()
      return result
    } catch (e: Exception) {
      failureCounter.increment()
      throw e
    }
  }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Event Payload for GDX")
data class EventToPublish(
  @Schema(
    description = "Type of event",
    required = true,
    example = "DEATH_NOTIFICATION",
    allowableValues = ["DEATH_NOTIFICATION", "LIFE_EVENT"],
  )
  val eventType: String,
  @Schema(
    description = "Date and time when the event took place, default is now",
    required = false,
    type = "date-time",
    example = "2021-12-31T12:34:56",
  )
  @DateTimeFormat(pattern = JacksonConfiguration.dateTimeFormat)
  val eventTime: LocalDateTime? = null,
  @Schema(description = "ID that references the event (optional)", required = false, example = "123456789")
  val id: String? = null,
  @Schema(description = "Json payload of data, normally no additional data would be sent", required = false)
  val eventDetails: String? = null,
)
