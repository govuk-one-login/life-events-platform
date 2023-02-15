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
import uk.gov.gdx.datashare.repositories.*
import java.util.*

@RestController
@RequestMapping("/data", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasAnyAuthority('SCOPE_events/admin')")
@Tag(name = "20. Data")
class BaseDataController(
  private val acquirerRepository: AcquirerRepository,
  private val acquirerSubscriptionRepository: AcquirerSubscriptionRepository,
  private val eventDataRepository: EventDataRepository,
  private val supplierRepository: SupplierRepository,
  private val supplierSubscriptionRepository: SupplierSubscriptionRepository,
) : BaseApiController() {
  @GetMapping("/acquirers")
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
        responseCode = "406",
        description = "Not able to process the request because the header “Accept” does not match with any of the content types this endpoint can handle",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
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
  fun getAcquirers() = acquirerRepository.findAll().toList()

  @DeleteMapping("/acquirers/{id}")
  @Operation(
    summary = "Delete Acquirer",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "Acquirer deleted",
      ),
      ApiResponse(
        responseCode = "406",
        description = "Not able to process the request because the header “Accept” does not match with any of the content types this endpoint can handle",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun deleteAcquirer(
    @Schema(description = "Acquirer ID", required = true, pattern = UUID_REGEX)
    @PathVariable
    id: UUID,
  ) = acquirerRepository.deleteById(id)

  @GetMapping("/acquirerSubscriptions")
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
            array = ArraySchema(schema = Schema(implementation = AcquirerSubscription::class)),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "406",
        description = "Not able to process the request because the header “Accept” does not match with any of the content types this endpoint can handle",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
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
  fun getAcquirerSubscriptions() = acquirerSubscriptionRepository.findAll().toList()

  @DeleteMapping("/acquirerSubscriptions/{id}")
  @Operation(
    summary = "Delete Acquirer Subscription",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "Acquirer Subscription deleted",
      ),
      ApiResponse(
        responseCode = "406",
        description = "Not able to process the request because the header “Accept” does not match with any of the content types this endpoint can handle",
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
    @Schema(description = "Acquirer Subscription ID", required = true, pattern = UUID_REGEX)
    @PathVariable
    id: UUID,
  ) = acquirerSubscriptionRepository.deleteById(id)

  @GetMapping("/events")
  @Operation(
    summary = "Get Events",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Event",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = EventData::class)),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "406",
        description = "Not able to process the request because the header “Accept” does not match with any of the content types this endpoint can handle",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
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
  fun getEvents() = eventDataRepository.findAll().toList()

  @DeleteMapping("/events/{id}")
  @Operation(
    summary = "Delete Event",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "Event deleted",
      ),
      ApiResponse(
        responseCode = "406",
        description = "Not able to process the request because the header “Accept” does not match with any of the content types this endpoint can handle",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun deleteEvent(
    @Schema(description = "Event ID", required = true, pattern = UUID_REGEX)
    @PathVariable
    id: UUID,
  ) = eventDataRepository.deleteById(id)

  @GetMapping("/suppliers")
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
        responseCode = "406",
        description = "Not able to process the request because the header “Accept” does not match with any of the content types this endpoint can handle",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
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
  fun getSuppliers() = supplierRepository.findAll().toList()

  @DeleteMapping("/suppliers/{id}")
  @Operation(
    summary = "Delete Supplier",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "Supplier deleted",
      ),
      ApiResponse(
        responseCode = "406",
        description = "Not able to process the request because the header “Accept” does not match with any of the content types this endpoint can handle",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun deleteSupplier(
    @Schema(description = "Supplier ID", required = true, pattern = UUID_REGEX)
    @PathVariable
    id: UUID,
  ) = supplierRepository.deleteById(id)

  @GetMapping("/supplierSubscriptions")
  @Operation(
    summary = "Get Supplier Subscriptions",
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
        responseCode = "406",
        description = "Not able to process the request because the header “Accept” does not match with any of the content types this endpoint can handle",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
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
  fun getSupplierSubscriptions() = supplierSubscriptionRepository.findAll().toList()

  @DeleteMapping("/supplierSubscriptions/{id}")
  @Operation(
    summary = "Delete Supplier Subscription",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "Supplier Subscription deleted",
      ),
      ApiResponse(
        responseCode = "406",
        description = "Not able to process the request because the header “Accept” does not match with any of the content types this endpoint can handle",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun deleteSupplierSubscription(
    @Schema(description = "Supplier Subscription ID", required = true, pattern = UUID_REGEX)
    @PathVariable
    id: UUID,
  ) = supplierSubscriptionRepository.deleteById(id)
}
