package uk.gov.gdx.datashare.resource

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import kotlinx.coroutines.flow.Flow
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.gdx.datashare.service.EventPollService
import java.time.LocalDateTime

@RestController
@RequestMapping("/events", produces = [ MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasAnyAuthority('SCOPE_events/poll')")
class EventPlatform(
  private val eventPollService: EventPollService
) {

  @GetMapping()
  @Operation(
    summary = "Returns all events for this client since the last call",
    description = "Need scope of events/poll",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Data Returned"
      )
    ]
  )
  suspend fun getEvents(
    @Schema(description = "Event Types required, if none supplied it will be the allowed types for this client", required = false)
    @RequestParam(name = "eventType") eventTypes: List<EventType> = listOf(),
    @Schema(description = "Events after this time, if not supplied it will be from the last time this endpoint was called for this client", type = "date-time", required = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'H:mm:ss")
    @RequestParam fromTime: LocalDateTime? = null,
    @Schema(description = "Events before this time, if not supplied it will be now", type = "date-time", required = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @RequestParam toTime: LocalDateTime? = null
  ): Flow<SubscribedEvent> = eventPollService.getEvents(eventTypes, fromTime, toTime)
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Subscribed event notification")
data class SubscribedEvent(
  @Schema(description = "Event ID (UUID)", type = "UUID", pattern = "^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}\$", required = true, example = "d8a6f3ba-e915-4e79-8479-f5f5830f4622")
  val eventId: String,
  @Schema(description = "Events Type", required = true, example = "DEATH_NOTIFICATION")
  val eventType: EventType,
  @Schema(description = "Events Time", type = "date-time", required = true)
  val eventTime: LocalDateTime
)
