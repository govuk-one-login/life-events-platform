package uk.gov.gdx.datashare.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import kotlinx.coroutines.flow.toList
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import uk.gov.gdx.datashare.repository.EgressEventDataRepository
import uk.gov.gdx.datashare.service.ConsumerRequest
import uk.gov.gdx.datashare.service.PublisherRequest
import uk.gov.gdx.datashare.service.SubscriptionManagerService

@RestController
@RequestMapping("/core-data", produces = [ MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasAnyAuthority('SCOPE_pubsub/maintain')")
@Validated
class PubSubProviderManager(
  private val subscriptionManagerService: SubscriptionManagerService,
  private val egressEventDataRepository: EgressEventDataRepository,
  private val ingressEventDataRepository: IngressEventDataRepository
) {

  @GetMapping("/pub")
  @Operation(
    summary = "Get Publishers",
    description = "Need scope of pubsub/maintain",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Publishers"
      )
    ]
  )
  suspend fun getPublishers() = subscriptionManagerService.getPublishers()

  @PostMapping("/pub")
  @Operation(
    summary = "Add Publisher",
    description = "Need scope of pubsub/maintain",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Publisher Added"
      )
    ]
  )
  suspend fun addPublisher(
    @Schema(
      description = "Publisher",
      required = true,
      implementation = PublisherRequest::class,
    )
    @RequestBody publisherRequest: PublisherRequest,
  ) = subscriptionManagerService.addPublisher(publisherRequest)

  @GetMapping("/sub")
  @Operation(
    summary = "Get Consumers",
    description = "Need scope of pubsub/maintain",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Consumers"
      )
    ]
  )
  suspend fun getConsumers() = subscriptionManagerService.getConsumers()

  @PostMapping("/sub")
  @Operation(
    summary = "Add Consumer",
    description = "Need scope of pubsub/maintain",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Consumer Added"
      )
    ]
  )
  suspend fun addConsumer(
    @Schema(
      description = "Consumer",
      required = true,
      implementation = ConsumerRequest::class,
    )
    @RequestBody consumerRequest: ConsumerRequest,
  ) = subscriptionManagerService.addConsumer(consumerRequest)

  @GetMapping("/egressEvents")
  @Operation(
    summary = "Get Egress Events",
    description = "Need scope of pubsub/maintain",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Egress Event"
      )
    ]
  )
  suspend fun getEgressEvents() = egressEventDataRepository.findAll().toList()

  @GetMapping("/ingressEvents")
  @Operation(
    summary = "Get Ingress Events",
    description = "Need scope of pubsub/maintain",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Ingress Event"
      )
    ]
  )
  suspend fun getIngressEvents() = ingressEventDataRepository.findAll().toList()
}
