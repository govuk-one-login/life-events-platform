package uk.gov.gdx.datashare.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.gdx.datashare.config.ErrorResponse
import uk.gov.gdx.datashare.enums.RegExConstants.UUID_REGEX
import uk.gov.gdx.datashare.models.SupplierRequest
import uk.gov.gdx.datashare.models.SupplierSubRequest
import uk.gov.gdx.datashare.repositories.Supplier
import uk.gov.gdx.datashare.repositories.SupplierSubscription
import uk.gov.gdx.datashare.services.SuppliersService
import java.util.*

@RestController
@RequestMapping("/suppliers", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasAnyAuthority('SCOPE_events/admin')")
@Tag(name = "12. Suppliers")
class SuppliersController(
  private val suppliersService: SuppliersService,
) : BaseApiController() {
  @GetMapping
  @Operation(
    summary = "Get Suppliers",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Suppliers",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = Supplier::class)),
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
  fun getSuppliers() = suppliersService.getSuppliers()

  @PostMapping
  @Operation(
    summary = "Add Supplier",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Supplier Added",
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
  fun addSupplier(
    @Schema(
      description = "Supplier",
      required = true,
      implementation = SupplierRequest::class,
    )
    @RequestBody
    supplierRequest: SupplierRequest,
  ) = suppliersService.addSupplier(supplierRequest)

  @DeleteMapping("/{supplierId}")
  @Operation(
    summary = "Delete Supplier",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Supplier Deleted",
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
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun deleteSupplier(
    @Schema(
      description = "Supplier ID",
      required = true,
    )
    @PathVariable
    supplierId: UUID,
  ) = suppliersService.deleteSupplier(supplierId)

  @GetMapping("/subscriptions")
  @Operation(
    summary = "Get Supplier Subscriptions",
    description = "Mark a supplier as deleted. This will also soft delete all supplier subscriptions linked to this supplier, and delete the cognito clients for those subscriptions, if no more subscriptions are attached to them.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Supplier Subscriptions",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = SupplierSubscription::class)),
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
  fun getSupplierSubscriptions() = suppliersService.getSupplierSubscriptions()

  @DeleteMapping("/subscriptions/{supplierSubscriptionId}")
  @Operation(
    summary = "Delete Supplier Subscriptions",
    description = "Mark a supplier subscription as deleted. This will also delete the cognito client for this subscription, if no more subscriptions are attached to it.",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Supplier Subscription Deleted",
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
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun deleteSupplierSubscription(
    @Schema(
      description = "Supplier Subscription ID",
      required = true,
    )
    @PathVariable
    supplierSubscriptionId: UUID,
  ) = suppliersService.deleteSupplierSubscription(supplierSubscriptionId)

  @GetMapping("/{supplierId}/subscriptions")
  @Operation(
    summary = "Get Supplier Subscriptions for Supplier",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Supplier Subscriptions",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = SupplierSubscription::class)),
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
  fun getSubscriptionsForSupplier(
    @Schema(
      description = "Supplier ID",
      required = true,
      example = "00000000-0000-0001-0000-000000000000",
      pattern = UUID_REGEX,
    )
    @PathVariable
    supplierId: UUID,
  ) = suppliersService.getSubscriptionsForSupplier(supplierId)

  @PostMapping("/{supplierId}/subscriptions")
  @Operation(
    summary = "Add Supplier Subscription",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Supplier Subscription Added",
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
  fun addSupplierSubscription(
    @Schema(
      description = "Supplier ID",
      required = true,
      example = "00000000-0000-0001-0000-000000000000",
      pattern = UUID_REGEX,
    )
    @PathVariable
    supplierId: UUID,
    @Schema(
      description = "Supplier Subscription",
      required = true,
      implementation = SupplierSubRequest::class,
    )
    @RequestBody
    supplierSubRequest: SupplierSubRequest,
  ) = suppliersService.addSupplierSubscription(supplierId, supplierSubRequest)

  @PutMapping("/{supplierId}/subscriptions/{subscriptionId}")
  @Operation(
    summary = "Update Supplier",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Supplier Subscription Updated",
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
  fun updateSupplierSubscription(
    @Schema(
      description = "Supplier ID",
      required = true,
      example = "00000000-0000-0001-0000-000000000000",
      pattern = UUID_REGEX,
    )
    @PathVariable
    supplierId: UUID,
    @Schema(
      description = "Supplier Subscription ID",
      required = true,
      example = "00000000-0000-0001-0000-000000000000",
      pattern = UUID_REGEX,
    )
    @PathVariable
    subscriptionId: UUID,
    @Schema(
      description = "Supplier Subscription to update",
      required = true,
      implementation = SupplierSubRequest::class,
    )
    @RequestBody
    supplierSubRequest: SupplierSubRequest,
  ) = suppliersService.updateSupplierSubscription(supplierId, subscriptionId, supplierSubRequest)
}
