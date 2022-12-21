package uk.gov.gdx.datashare.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import kotlinx.coroutines.flow.toList
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.gdx.datashare.service.ConsumerSubRequest
import uk.gov.gdx.datashare.service.PublisherSubRequest
import uk.gov.gdx.datashare.service.SubscriptionManagerService
import java.util.UUID

@RestController
@RequestMapping("/subscriptions", produces = [ MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasAnyAuthority('SCOPE_subscriptions/maintain')")
@Validated
class SubscriptionManager(
  private val subscriptionManagerService: SubscriptionManagerService
) {

  @GetMapping("/events")
  @Operation(
    summary = "Get Event Subscriptions",
    description = "Need scope of subscriptions/maintain",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Event Subscriptions"
      )
    ]
  )
  suspend fun getEventSubscriptions() = subscriptionManagerService.getEventSubscriptions().toList()

  @GetMapping("/consumers")
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
  suspend fun getConsumerSubscriptions() = subscriptionManagerService.getConsumersSubscriptions()

  @PostMapping("/event")
  @Operation(
    summary = "Add Event Subscription",
    description = "Need scope of subscriptions/maintain",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Event Subscription Added"
      )
    ]
  )
  suspend fun addEventSubscription(
    @Schema(
      description = "Event Subscription",
      required = true,
      implementation = PublisherSubRequest::class,
    )
    @RequestBody publisherSubRequest: PublisherSubRequest,
  ) = subscriptionManagerService.addEventSubscription(publisherSubRequest)

  @PutMapping("/event/{subscriptionId}")
  @Operation(
    summary = "Update Event Subscription",
    description = "Need scope of subscriptions/maintain",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Event Subscription Updated"
      )
    ]
  )
  suspend fun updateEventSubscription(
    @Schema(description = "Event Subscription ID", required = true)
    @PathVariable subscriptionId: UUID,
    @Schema(
      description = "Event Subscription to update",
      required = true,
      implementation = PublisherSubRequest::class,
    )
    @RequestBody publisherSubRequest: PublisherSubRequest,
  ) = subscriptionManagerService.updateEventSubscription(subscriptionId, publisherSubRequest)

  @PostMapping("/consumer")
  @Operation(
    summary = "Add Consumer",
    description = "Need scope of subscriptions/maintain",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Consumer Subscription Added"
      )
    ]
  )
  suspend fun addConsumerSubscription(
    @Schema(
      description = "Consumer Subscription",
      required = true,
      implementation = ConsumerSubRequest::class,
    )
    @RequestBody consumerSubRequest: ConsumerSubRequest,
  ) = subscriptionManagerService.addConsumerSubscription(consumerSubRequest)

  @PutMapping("/consumer/{subscriptionId}")
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
    @Schema(description = "Consumer Subscription ID", required = true)
    @PathVariable subscriptionId: UUID,
    @Schema(
      description = "Consumer Subscription to update",
      required = true,
      implementation = ConsumerSubRequest::class,
    )
    @RequestBody consumerSubRequest: ConsumerSubRequest,
  ) = subscriptionManagerService.updateConsumerSubscription(subscriptionId, consumerSubRequest)
}
