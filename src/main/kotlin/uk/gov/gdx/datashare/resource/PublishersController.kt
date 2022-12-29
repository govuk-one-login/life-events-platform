package uk.gov.gdx.datashare.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import uk.gov.gdx.datashare.service.PublisherRequest
import uk.gov.gdx.datashare.service.PublisherSubRequest
import uk.gov.gdx.datashare.service.PublishersService
import java.util.*

@RestController
@RequestMapping("/publishers", produces = [ MediaType.APPLICATION_JSON_VALUE])
@Validated
class PublishersController(
  private val publishersService: PublishersService
) {
  @PreAuthorize("hasAnyAuthority('SCOPE_pubsub/maintain')")
  @GetMapping
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
  suspend fun getPublishers() = publishersService.getPublishers()

  @PreAuthorize("hasAnyAuthority('SCOPE_pubsub/maintain')")
  @PostMapping
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
    @RequestBody  publisherRequest: PublisherRequest,
  ) = publishersService.addPublisher(publisherRequest)

  @PreAuthorize("hasAnyAuthority('SCOPE_pubsub/maintain')")
  @GetMapping("/subscriptions")
  @Operation(
    summary = "Get Publisher Subscriptions",
    description = "Need scope of subscriptions/maintain",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Publisher Subscriptions"
      )
    ]
  )
  suspend fun getPublisherSubscriptions() = publishersService.getPublisherSubscriptions()

  @PreAuthorize("hasAnyAuthority('SCOPE_subscriptions/maintain')")
  @GetMapping("/{publisherId}/subscriptions")
  @Operation(
    summary = "Get Publisher Subscriptions for Publisher",
    description = "Need scope of subscriptions/maintain",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Publisher Subscriptions"
      )
    ]
  )
  suspend fun getSubscriptionsForPublisher(
    @Schema(description = "Publisher ID", required = true, example = "00000000-0000-0001-0000-000000000000")
    @PathVariable  publisherId: UUID
  ) = publishersService.getSubscriptionsForPublisher(publisherId)

  @PreAuthorize("hasAnyAuthority('SCOPE_subscriptions/maintain')")
  @PostMapping("/{publisherId}/subscriptions")
  @Operation(
    summary = "Add Publisher Subscription",
    description = "Need scope of pubsub/maintain",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Publisher Subscription Added"
      )
    ]
  )
  suspend fun addPublisherSubscription(
    @Schema(description = "Publisher ID", required = true, example = "00000000-0000-0001-0000-000000000000")
    @PathVariable  publisherId: UUID,
    @Schema(
      description = "Publisher Subscription",
      required = true,
      implementation = PublisherSubRequest::class,
    )
    @RequestBody  publisherSubRequest: PublisherSubRequest,
  ) = publishersService.addPublisherSubscription(publisherId, publisherSubRequest)

  @PreAuthorize("hasAnyAuthority('SCOPE_subscriptions/maintain')")
  @PutMapping("/{publisherId}/subscriptions/{subscriptionId}")
  @Operation(
    summary = "Update Publisher",
    description = "Need scope of subscriptions/maintain",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Publisher Subscription Updated"
      )
    ]
  )
  suspend fun updatePublisherSubscription(
    @Schema(description = "Publisher ID", required = true, example = "00000000-0000-0001-0000-000000000000")
    @PathVariable publisherId: UUID,
    @Schema(description = "Publisher Subscription ID", required = true, example = "00000000-0000-0001-0000-000000000000")
    @PathVariable subscriptionId: UUID,
    @Schema(
      description = "Publisher Subscription to update",
      required = true,
      implementation = PublisherSubRequest::class,
    )
    @RequestBody  publisherSubRequest: PublisherSubRequest,
  ) = publishersService.updatePublisherSubscription(publisherId, subscriptionId, publisherSubRequest)
}