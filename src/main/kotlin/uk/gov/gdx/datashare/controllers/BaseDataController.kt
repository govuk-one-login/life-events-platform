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
import uk.gov.gdx.datashare.repositories.*
import java.util.*

@RestController
@RequestMapping("/data", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasAnyAuthority('SCOPE_events/admin')")
@Validated
@XRayEnabled
@Tag(name = "20. Data")
class BaseDataController(
  private val acquirerRepository: AcquirerRepository,
  private val acquirerSubscriptionRepository: AcquirerSubscriptionRepository,
  private val eventDataRepository: EventDataRepository,
  private val supplierRepository: SupplierRepository,
  private val supplierSubscriptionRepository: SupplierSubscriptionRepository,
) {
  @GetMapping("/acquirers")
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
  fun getAcquirers() = acquirerRepository.findAll()

  @DeleteMapping("/acquirers/{id}")
  @Operation(
    summary = "Delete Acquirer",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "Acquirer deleted",
      ),
    ],
  )
  fun deleteAcquirer(
    @Schema(description = "Acquirer ID", required = true)
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
      ),
    ],
  )
  fun getAcquirerSubscriptions() = acquirerSubscriptionRepository.findAll()

  @DeleteMapping("/acquirerSubscriptions/{id}")
  @Operation(
    summary = "Delete Acquirer Subscription",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "Acquirer Subscription deleted",
      ),
    ],
  )
  fun deleteAcquirerSubscription(
    @Schema(description = "Acquirer Subscription ID", required = true)
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
      ),
    ],
  )
  fun getEvents() = eventDataRepository.findAll()

  @DeleteMapping("/events/{id}")
  @Operation(
    summary = "Delete Event",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "Event deleted",
      ),
    ],
  )
  fun deleteEvent(
    @Schema(description = "Event ID", required = true)
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
      ),
    ],
  )
  fun getSuppliers() = supplierRepository.findAll()

  @DeleteMapping("/suppliers/{id}")
  @Operation(
    summary = "Delete Supplier",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "Supplier deleted",
      ),
    ],
  )
  fun deleteSupplier(
    @Schema(description = "Supplier ID", required = true)
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
      ),
    ],
  )
  fun getSupplierSubscriptions() = supplierSubscriptionRepository.findAll()

  @DeleteMapping("/supplierSubscriptions/{id}")
  @Operation(
    summary = "Delete Supplier Subscription",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "Supplier Subscription deleted",
      ),
    ],
  )
  fun deleteSupplierSubscription(
    @Schema(description = "Supplier Subscription ID", required = true)
    @PathVariable
    id: UUID,
  ) = supplierSubscriptionRepository.deleteById(id)
}
