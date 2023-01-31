package uk.gov.gdx.datashare.controllers

import com.amazonaws.xray.spring.aop.XRayEnabled
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import uk.gov.gdx.datashare.models.AcquirerRequest
import uk.gov.gdx.datashare.models.AcquirerSubRequest
import uk.gov.gdx.datashare.repositories.Acquirer
import uk.gov.gdx.datashare.services.AcquirersService
import java.util.*

@RestController
@RequestMapping("/acquirers", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasAnyAuthority('SCOPE_events/admin')")
@Validated
@XRayEnabled
@Tag(name = "11. Acquirers")
class AcquirersController(
  private val acquirersService: AcquirersService,
) {
  @GetMapping
  @Operation(
    summary = "Get Acquirers",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Acquirers",
      ),
    ],
  )
  fun getAcquirers(): MutableIterable<Acquirer> = acquirersService.getAcquirers()

  @PostMapping
  @Operation(
    summary = "Add Acquirer",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "AcquirerAdded",
      ),
    ],
  )
  fun addAcquirer(
    @Schema(
      description = "Acquirer",
      required = true,
      implementation = AcquirerRequest::class,
    )
    @RequestBody
    acquirerRequest: AcquirerRequest,
  ) = acquirersService.addAcquirer(acquirerRequest)

  @GetMapping("/subscriptions")
  @Operation(
    summary = "Get Acquirer Subscriptions",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Acquirer Subscriptions",
      ),
    ],
  )
  fun getAcquirerSubscriptions() = acquirersService.getAcquirerSubscriptions()

  @GetMapping("/{acquirerId}/subscriptions")
  @Operation(
    summary = "Get Acquirer Subscriptions for Acquirer",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Acquirer Subscriptions",
      ),
    ],
  )
  fun getSubscriptionsForAcquirer(
    @Schema(description = "Acquirer ID", required = true, example = "00000000-0000-0001-0000-000000000000")
    @PathVariable
    acquirerId: UUID,
  ) = acquirersService.getSubscriptionsForAcquirer(acquirerId)

  @PostMapping("/{acquirerId}/subscriptions")
  @Operation(
    summary = "Add Acquirer Subscription",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Acquirer Subscription Added",
      ),
    ],
  )
  fun addAcquirerSubscription(
    @Schema(description = "Acquirer ID", required = true, example = "00000000-0000-0001-0000-000000000000")
    @PathVariable
    acquirerId: UUID,
    @Schema(
      description = "Acquirer Subscription",
      required = true,
      implementation = AcquirerSubRequest::class,
    )
    @RequestBody
    acquirerSubRequest: AcquirerSubRequest,
  ) = acquirersService.addAcquirerSubscription(acquirerId, acquirerSubRequest)

  @PutMapping("/{acquirerId}/subscriptions/{subscriptionId}")
  @Operation(
    summary = "Update Acquirer",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Acquirer Subscription Updated",
      ),
    ],
  )
  fun updateAcquirerSubscription(
    @Schema(description = "Acquirer ID", required = true, example = "00000000-0000-0001-0000-000000000000")
    @PathVariable
    acquirerId: UUID,
    @Schema(description = "Acquirer Subscription ID", required = true, example = "00000000-0000-0001-0000-000000000000")
    @PathVariable
    subscriptionId: UUID,
    @Schema(
      description = "Acquirer Subscription to update",
      required = true,
      implementation = AcquirerSubRequest::class,
    )
    @RequestBody
    acquirerSubRequest: AcquirerSubRequest,
  ) = acquirersService.updateAcquirerSubscription(acquirerId, subscriptionId, acquirerSubRequest)
}
