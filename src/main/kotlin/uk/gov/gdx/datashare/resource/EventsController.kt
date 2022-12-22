package uk.gov.gdx.datashare.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import uk.gov.gdx.datashare.service.EventDataService
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/events", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasAnyAuthority('SCOPE_data_retriever/read')")
@Validated
class EventsController(
  private val eventDataService: EventDataService
) {
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

  @GetMapping
  @Operation(
    summary = "Event Get API - Get event data",
    description = "Get all events for consumer, Need scope of data_retriever/read",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "Event deleted"
      )
    ]
  )
  suspend fun getEvents(
    @Schema(
      description = "Event Types required, if none supplied it will be the allowed types for this client",
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
  ) {
    eventDataService.getEvents(eventTypes, startTime, endTime)
  }
}