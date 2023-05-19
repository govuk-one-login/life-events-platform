package uk.gov.gdx.datashare.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.gdx.datashare.config.ErrorResponse
import uk.gov.gdx.datashare.enums.RegExConstants.UUID_REGEX
import uk.gov.gdx.datashare.models.AcquirerRequest
import uk.gov.gdx.datashare.models.AcquirerSubRequest
import uk.gov.gdx.datashare.models.AcquirerSubscriptionDto
import uk.gov.gdx.datashare.repositories.Acquirer
import uk.gov.gdx.datashare.services.AcquirersService
import java.util.*

@RestController
@RequestMapping("/acquirers", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasAnyAuthority('SCOPE_events/admin')")
@Tag(name = "11. Acquirers")
class AcquirersController(
  private val acquirersService: AcquirersService,
) : BaseApiController() {
  @GetMapping
  @Operation(
    summary = "Get Acquirers",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Acquirers",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = Acquirer::class)),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "415",
        description = "Not able to process the request because the payload is in a format not supported by this endpoint.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
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
      ApiResponse(
        responseCode = "415",
        description = "Not able to process the request because the payload is in a format not supported by this endpoint.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
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
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = AcquirerSubscriptionDto::class)),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "415",
        description = "Not able to process the request because the payload is in a format not supported by this endpoint.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun getAcquirerSubscriptions() = acquirersService.getAcquirerSubscriptions()

  @DeleteMapping("/subscriptions")
  @Operation(
    summary = "Delete Acquirer Subscriptions",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Acquirer Subscription Deleted",
      ),
      ApiResponse(
        responseCode = "415",
        description = "Not able to process the request because the payload is in a format not supported by this endpoint.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun deleteAcquirerSubscription(
    @Schema(
      description = "Acquirer Subscription ID",
      required = true,
    )
    @RequestBody
    acquirerSubscriptionId: UUID,
  ) = acquirersService.deleteAcquirerSubscription(acquirerSubscriptionId)

  @GetMapping("/{acquirerId}/subscriptions")
  @Operation(
    summary = "Get Acquirer Subscriptions for Acquirer",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Acquirer Subscriptions",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = AcquirerSubscriptionDto::class)),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "415",
        description = "Not able to process the request because the payload is in a format not supported by this endpoint.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun getSubscriptionsForAcquirer(
    @Schema(description = "Acquirer ID", required = true, example = "00000000-0000-0001-0000-000000000000", pattern = UUID_REGEX)
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
      ApiResponse(
        responseCode = "415",
        description = "Not able to process the request because the payload is in a format not supported by this endpoint.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun addAcquirerSubscription(
    @Schema(description = "Acquirer ID", required = true, example = "00000000-0000-0001-0000-000000000000", pattern = UUID_REGEX)
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
      ApiResponse(
        responseCode = "415",
        description = "Not able to process the request because the payload is in a format not supported by this endpoint.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun updateAcquirerSubscription(
    @Schema(description = "Acquirer ID", required = true, example = "00000000-0000-0001-0000-000000000000", pattern = UUID_REGEX)
    @PathVariable
    acquirerId: UUID,
    @Schema(description = "Acquirer Subscription ID", required = true, example = "00000000-0000-0001-0000-000000000000", pattern = UUID_REGEX)
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
