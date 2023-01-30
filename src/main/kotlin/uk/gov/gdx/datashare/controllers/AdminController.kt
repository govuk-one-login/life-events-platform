package uk.gov.gdx.datashare.controllers

import com.amazonaws.xray.spring.aop.XRayEnabled
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.gdx.datashare.enums.CognitoClientType
import uk.gov.gdx.datashare.models.CognitoClientRequest
import uk.gov.gdx.datashare.models.CognitoClientResponse
import uk.gov.gdx.datashare.models.ConsumerRequest
import uk.gov.gdx.datashare.models.ConsumerSubRequest
import uk.gov.gdx.datashare.models.CreateAcquirerRequest
import uk.gov.gdx.datashare.repositories.EventDataRepository
import uk.gov.gdx.datashare.services.CognitoService
import uk.gov.gdx.datashare.services.ConsumersService

@RestController
@RequestMapping("/admin", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasAnyAuthority('SCOPE_events/admin')")
@Validated
@XRayEnabled
@Tag(name = "13. Admin")
class AdminController(
  private val eventDataRepository: EventDataRepository,
  private val cognitoService: CognitoService,
  private val consumersService: ConsumersService,
) {
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
  ) = cognitoService.createUserPoolClient(cognitoClientRequest)

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
    createAcquirerRequest: CreateAcquirerRequest,
  ) = cognitoService.createUserPoolClient(
    CognitoClientRequest(createAcquirerRequest.clientName, listOf(CognitoClientType.ACQUIRER)),
  )?.let {
    val consumer = consumersService.addConsumer(ConsumerRequest(createAcquirerRequest.clientName))
    consumersService.addConsumerSubscription(
      consumer.id,
      ConsumerSubRequest(
        oauthClientId = it.clientId,
        eventType = createAcquirerRequest.eventType,
        enrichmentFields = createAcquirerRequest.enrichmentFields,
        enrichmentFieldsIncludedInPoll = createAcquirerRequest.enrichmentFieldsIncludedInPoll,
      ),
    )

    CognitoClientResponse(it.clientName, it.clientId, it.clientSecret)
  }
}
