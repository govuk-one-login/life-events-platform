package uk.gov.gdx.datashare.controllers

import com.toedter.spring.hateoas.jsonapi.MediaTypes
import io.micrometer.core.instrument.MeterRegistry
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.gdx.datashare.models.EventToPublish
import uk.gov.gdx.datashare.services.DataReceiverService

@RestController
@RequestMapping("/events", produces = [MediaTypes.JSON_API_VALUE])
@PreAuthorize("hasAnyAuthority('SCOPE_events/publish')")
@Validated
@Tag(name = "02. Supplier")
class SupplierController(
  private val dataReceiverService: DataReceiverService,
  private val meterRegistry: MeterRegistry,
) : BaseController {
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
}
