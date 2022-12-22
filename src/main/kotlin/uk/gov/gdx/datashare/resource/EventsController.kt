package uk.gov.gdx.datashare.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import uk.gov.gdx.datashare.service.EventDataService
import java.util.*

@RestController
@RequestMapping("/events", produces = [ MediaType.APPLICATION_JSON_VALUE])
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
}