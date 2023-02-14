package uk.gov.gdx.datashare.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.gdx.datashare.config.ErrorResponse
import uk.gov.gdx.datashare.models.CognitoClientRequest
import uk.gov.gdx.datashare.models.CreateAcquirerRequest
import uk.gov.gdx.datashare.models.CreateSupplierRequest
import uk.gov.gdx.datashare.repositories.EventData
import uk.gov.gdx.datashare.repositories.EventDataRepository
import uk.gov.gdx.datashare.services.AdminService

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

  @PostMapping("/cognitoClients")
  @Operation(
    summary = "Create cognito client",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Cognito client details",
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
