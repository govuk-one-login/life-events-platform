package uk.gov.gdx.datashare.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import uk.gov.gdx.datashare.service.ConsumerRequest
import uk.gov.gdx.datashare.service.ConsumerSubRequest
import uk.gov.gdx.datashare.service.ConsumersService
import java.util.*

@RestController
@RequestMapping("/consumers", produces = [ MediaType.APPLICATION_JSON_VALUE])
@Validated
class ConsumersController(
  private val consumersService: ConsumersService
) {
  @PreAuthorize("hasAnyAuthority('SCOPE_pubsub/maintain')")
  @GetMapping
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
  suspend fun getConsumers() = consumersService.getConsumers()

  @PreAuthorize("hasAnyAuthority('SCOPE_pubsub/maintain')")
  @PostMapping
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
  ) = consumersService.addConsumer(consumerRequest)

  @PreAuthorize("hasAnyAuthority('SCOPE_pubsub/maintain')")
  @GetMapping("/subscriptions")
  @Operation(
    summary = "Get Consumer Subscriptions",
    description = "Need scope of subscriptions/maintain",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Consumer Subscriptions"
      )
    ]
  )
  suspend fun getConsumerSubscriptions() = consumersService.getConsumerSubscriptions()

  @PreAuthorize("hasAnyAuthority('SCOPE_subscriptions/maintain')")
  @GetMapping("/{consumerId}/subscriptions")
  @Operation(
    summary = "Get Consumer Subscriptions for Consumer",
    description = "Need scope of subscriptions/maintain",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Consumer Subscriptions"
      )
    ]
  )
  suspend fun getSubscriptionsForConsumer(
    @Schema(description = "Consumer ID", required = true, example = "00000000-0000-0001-0000-000000000000")
    @PathVariable consumerId: UUID
  ) = consumersService.getSubscriptionsForConsumer(consumerId)

  @PreAuthorize("hasAnyAuthority('SCOPE_subscriptions/maintain')")
  @PostMapping("/{consumerId}/subscriptions")
  @Operation(
    summary = "Add Consumer Subscription",
    description = "Need scope of pubsub/maintain",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Consumer Subscription Added"
      )
    ]
  )
  suspend fun addConsumerSubscription(
    @Schema(description = "Consumer ID", required = true, example = "00000000-0000-0001-0000-000000000000")
    @PathVariable consumerId: UUID,
    @Schema(
      description = "Consumer Subscription",
      required = true,
      implementation = ConsumerSubRequest::class,
    )
    @RequestBody consumerSubRequest: ConsumerSubRequest,
  ) = consumersService.addConsumerSubscription(consumerId, consumerSubRequest)

  @PreAuthorize("hasAnyAuthority('SCOPE_subscriptions/maintain')")
  @PutMapping("/{consumerId}/subscriptions/{subscriptionId}")
  @Operation(
    summary = "Update Consumer",
    description = "Need scope of subscriptions/maintain",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Consumer Subscription Updated"
      )
    ]
  )
  suspend fun updateConsumerSubscription(
    @Schema(description = "Consumer ID", required = true, example = "00000000-0000-0001-0000-000000000000")
    @PathVariable consumerId: UUID,
    @Schema(description = "Consumer Subscription ID", required = true, example = "00000000-0000-0001-0000-000000000000")
    @PathVariable subscriptionId: UUID,
    @Schema(
      description = "Consumer Subscription to update",
      required = true,
      implementation = ConsumerSubRequest::class,
    )
    @RequestBody consumerSubRequest: ConsumerSubRequest,
  ) = consumersService.updateConsumerSubscription(consumerId, subscriptionId, consumerSubRequest)
}