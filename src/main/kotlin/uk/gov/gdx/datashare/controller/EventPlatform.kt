package uk.gov.gdx.datashare.controller

import com.fasterxml.jackson.annotation.JsonInclude
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.flow.Flow
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.gdx.datashare.config.JacksonConfiguration
import uk.gov.gdx.datashare.service.EventPollService
import java.time.LocalDateTime
import java.util.UUID

@RestController
@RequestMapping("/obsolete/events", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasAnyAuthority('SCOPE_events/poll')")
@Validated
@Tag(name = "902. Event status")
class EventPlatform(
  private val eventPollService: EventPollService,
  meterRegistry: MeterRegistry,
) {
  private val callsToPollCounter: Counter = meterRegistry.counter("API_CALLS.CallsToPoll")

  @GetMapping
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
    @DateTimeFormat(pattern = JacksonConfiguration.dateTimeFormat)
    @RequestParam(name = "fromTime", required = false) fromTime: LocalDateTime? = null,
    @Schema(
      description = "Events before this time, if not supplied it will be now",
      type = "date-time",
      required = false
    )
    @DateTimeFormat(pattern = JacksonConfiguration.dateTimeFormat)
    @RequestParam(name = "toTime", required = false) toTime: LocalDateTime? = null
  ): Flow<SubscribedEvent> = run {
    callsToPollCounter.increment()
    eventPollService.getEvents(eventTypes, fromTime, toTime)
  }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Subscribed event notification")
data class SubscribedEvent(
  @Schema(description = "Event ID (UUID)", required = true, example = "d8a6f3ba-e915-4e79-8479-f5f5830f4622")
  val eventId: UUID,
  @Schema(
    description = "Events Type",
    required = true,
    example = "DEATH_NOTIFICATION",
    allowableValues = ["DEATH_NOTIFICATION", "LIFE_EVENT"]
  )
  val eventType: String,
  @Schema(description = "Events Time", type = "date-time", required = true, example = "2021-12-31T12:34:56.000Z")
  @DateTimeFormat(pattern = JacksonConfiguration.dateTimeFormat)
  val eventTime: LocalDateTime
)
