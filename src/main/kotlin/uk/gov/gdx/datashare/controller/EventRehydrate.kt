package uk.gov.gdx.datashare.controller

import com.fasterxml.jackson.annotation.JsonInclude
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.gdx.datashare.service.EventDataRetrievalService
import java.util.*

@RestController
@RequestMapping("/event-data-retrieval", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasAnyAuthority('SCOPE_data/consume')")
@Validated
@Tag(name = "91. Event data retrieval")
class EventRehydrate(
  private val eventDataRetrievalService: EventDataRetrievalService,
  meterRegistry: MeterRegistry
) {
  private val callsToEnrichCounter: Counter = meterRegistry.counter("API_CALLS.CallsToEnrich")

  @GetMapping("/{id}")
  @Operation(
    summary = "Event Rehydrate API - Lookup event data",
    description = "The event ID is the UUID received off the queue, Need scope of data/consume",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Data Returned"
      )
    ]
  )
  suspend fun getEventDetails(
    @Schema(description = "Event ID", required = true)
    @PathVariable id: UUID,
  ): EventInformation = run {
    callsToEnrichCounter.increment()
    eventDataRetrievalService.retrieveData(id)
  }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class EventInformation(
  @Schema(
    description = "Events Type",
    required = true,
    example = "DEATH_NOTIFICATION",
    allowableValues = ["DEATH_NOTIFICATION", "LIFE_EVENT"]
  )
  val eventType: String,
  @Schema(description = "Event ID (UUID)", required = true, example = "d8a6f3ba-e915-4e79-8479-f5f5830f4622")
  val eventId: UUID,
  @Schema(description = "Details of event, a payload of JSON data", required = false)
  val details: Any? = null,
)
