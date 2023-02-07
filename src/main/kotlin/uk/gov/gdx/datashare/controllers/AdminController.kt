package uk.gov.gdx.datashare.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.gdx.datashare.models.CognitoClientRequest
import uk.gov.gdx.datashare.models.CreateAcquirerRequest
import uk.gov.gdx.datashare.models.CreateSupplierRequest
import uk.gov.gdx.datashare.repositories.EventDataRepository
import uk.gov.gdx.datashare.services.AdminService
import javax.validation.Valid

@RestController
@RequestMapping("/admin", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasAnyAuthority('SCOPE_events/admin')")
@Tag(name = "13. Admin")
class AdminController(
  private val eventDataRepository: EventDataRepository,
  private val adminService: AdminService,
) : BaseApiController() {
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
  fun getEvents() = eventDataRepository.findAll().toList()

  @PostMapping("/cognitoClients")
  @Operation(
    summary = "Create cognito client",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Cognito client details",
      ),
    ],
  )
  fun createCognitoClient(
    @Schema(
      required = true,
      implementation = CognitoClientRequest::class,
    )
    @RequestBody
    cognitoClientRequest: CognitoClientRequest,
  ) = adminService.createCognitoClient(cognitoClientRequest)

  @PostMapping("/acquirer")
  @Operation(
    summary = "Create acquirer",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Cognito client details",
      ),
    ],
  )
  fun createAcquirer(
    @Schema(
      required = true,
      implementation = CreateAcquirerRequest::class,
    )
    @RequestBody
    @Valid
    createAcquirerRequest: CreateAcquirerRequest,
  ) = adminService.createAcquirer(createAcquirerRequest)

  @PostMapping("/supplier")
  @Operation(
    summary = "Create supplier",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Cognito client details",
      ),
    ],
  )
  fun createSupplier(
    @Schema(
      required = true,
      implementation = CreateSupplierRequest::class,
    )
    @RequestBody
    @Valid
    createSupplierRequest: CreateSupplierRequest,
  ) = adminService.createSupplier(createSupplierRequest)
}
