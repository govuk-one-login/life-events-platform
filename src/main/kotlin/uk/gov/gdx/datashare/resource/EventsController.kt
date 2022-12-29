package uk.gov.gdx.datashare.resource

import com.fasterxml.jackson.annotation.JsonInclude
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.flow.toList
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import uk.gov.gdx.datashare.service.DataReceiverService
import uk.gov.gdx.datashare.service.EventDataService
import uk.gov.gdx.datashare.service.EventNotification
import uk.gov.gdx.datashare.service.EventStatus
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/events", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasAnyAuthority('SCOPE_data_retriever/read')")
@Validated
@Tag(name = "4. Events")
class EventsController(
  private val eventDataService: EventDataService,
  private val dataReceiverService: DataReceiverService,
  meterRegistry: MeterRegistry,
) {
  private val callsToPollCounter: Counter = meterRegistry.counter("API_CALLS.CallsToPoll")
  private val ingestedEventsCounter: Counter = meterRegistry.counter("API_CALLS.IngestedEvents")

  @GetMapping("/status")
  @Operation(
    summary = "Event Get API - Get event status",
    description = "Get count of all events for consumer, Need scope of data_retriever/read",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Count per type"
      )
    ]
  )
  suspend fun getEventsStatus(
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'H:mm:ss")
    @RequestParam(name = "fromTime", required = false) startTime: LocalDateTime? = null,
    @Schema(
      description = "Events before this time, if not supplied it will be now",
      type = "date-time",
      required = false
    )
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @RequestParam(name = "toTime", required = false) endTime: LocalDateTime? = null
  ): List<EventStatus> = eventDataService.getEventsStatus(startTime, endTime).toList()

  @GetMapping
  @Operation(
    summary = "Event Get API - Get event data",
    description = "Get all events for consumer, Need scope of data_retriever/read",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Events"
      )
    ]
  )
  suspend fun getEvents(
    @Schema(
      description = "Event Types, if none supplied it will be the allowed types for this client",
      required = false,
      allowableValues = ["DEATH_NOTIFICATION", "LIFE_EVENT"]
    )
    @RequestParam(name = "eventType", required = false) eventTypes: List<String> = listOf(),
    @Schema(
      description = "Events after this time, if not supplied it will be from the last time this endpoint was called for this client",
      type = "date-time",
      required = false
    )
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'H:mm:ss")
    @RequestParam(name = "fromTime", required = false) startTime: LocalDateTime? = null,
    @Schema(
      description = "Events before this time, if not supplied it will be now",
      type = "date-time",
      required = false
    )
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @RequestParam(name = "toTime", required = false) endTime: LocalDateTime? = null
  ): List<EventNotification> = run {
    callsToPollCounter.increment()
    eventDataService.getEvents(eventTypes, startTime, endTime).toList()
  }

  @PreAuthorize("hasAnyAuthority('SCOPE_data_receiver/notify')")
  @PostMapping
  @Operation(
    summary = "Send ingress events to GDS - The 'Source' of the event - this could be HMPO or DWP for example",
    description = "Scope is data_receiver/notify",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Data Accepted"
      )
    ]
  )
  suspend fun publishEvent(
    @Schema(
      description = "Event Payload",
      required = true,
      implementation = EventToPublish::class,
    )
    @RequestBody eventPayload: EventToPublish,
  ) = run {
    dataReceiverService.sendToDataProcessor(eventPayload)
    ingestedEventsCounter.increment()
  }

  @DeleteMapping("/{id}")
  @Operation(
    summary = "Event Delete API - Delete event data",
    description = "The event ID is the UUID received off the queue, Need scope of data_retriever/read",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "Event deleted"
      )
    ]
  )
  suspend fun deleteEvent(
    @Schema(description = "Event ID", required = true)
    @PathVariable id: UUID,
  ): ResponseEntity<Void> {
    eventDataService.deleteEvent(id)
    return ResponseEntity<Void>(HttpStatus.NO_CONTENT)
  }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Event Payload for GDX")
data class EventToPublish(
  @Schema(description = "Type of event", required = true, example = "DEATH_NOTIFICATION", allowableValues = [ "DEATH_NOTIFICATION", "LIFE_EVENT"])
  val eventType: String,
  @Schema(description = "Date and time when the event took place, default is now", required = false, type = "date-time", example = "2021-12-31T12:34:56")
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'H:mm:ss")
  val eventTime: LocalDateTime? = null,
  @Schema(description = "ID that references the event (optional)", required = false, example = "123456789")
  val id: String? = null,
  @Schema(description = "Json payload of data, normally no additional data would be sent", required = false)
  val eventDetails: String? = null,
)