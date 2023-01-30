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
  private val consumerRepository: ConsumerRepository,
  private val consumerSubscriptionRepository: ConsumerSubscriptionRepository,
  private val eventDataRepository: EventDataRepository,
  private val publisherRepository: PublisherRepository,
  private val publisherSubscriptionRepository: PublisherSubscriptionRepository,
) {
  @GetMapping("/consumers")
  @Operation(
    summary = "Get Consumers",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Consumers",
      ),
    ],
  )
  fun getConsumers() = consumerRepository.findAll()

  @DeleteMapping("/consumers/{id}")
  @Operation(
    summary = "Delete Consumer",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "Consumer deleted",
      ),
    ],
  )
  fun deleteConsumer(
    @Schema(description = "Consumer ID", required = true)
    @PathVariable
    id: UUID,
  ) = consumerRepository.deleteById(id)

  @GetMapping("/consumerSubscriptions")
  @Operation(
    summary = "Get Consumer Subscriptions",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Consumer Subscriptions",
      ),
    ],
  )
  fun getConsumerSubscriptions() = consumerSubscriptionRepository.findAll()

  @DeleteMapping("/consumerSubscriptions/{id}")
  @Operation(
    summary = "Delete Consumer Subscription",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "Consumer Subscription deleted",
      ),
    ],
  )
  fun deleteConsumerSubscription(
    @Schema(description = "Consumer Subscription ID", required = true)
    @PathVariable
    id: UUID,
  ) = consumerSubscriptionRepository.deleteById(id)

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

  @GetMapping("/publishers")
  @Operation(
    summary = "Get Publishers",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Publishers",
      ),
    ],
  )
  fun getPublishers() = publisherRepository.findAll()

  @DeleteMapping("/publishers/{id}")
  @Operation(
    summary = "Delete Publisher",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "Publisher deleted",
      ),
    ],
  )
  fun deletePublisher(
    @Schema(description = "Publisher ID", required = true)
    @PathVariable
    id: UUID,
  ) = publisherRepository.deleteById(id)

  @GetMapping("/publisherSubscriptions")
  @Operation(
    summary = "Get Publisher Subscriptions",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Publisher Subscriptions",
      ),
    ],
  )
  fun getPublisherSubscriptions() = publisherSubscriptionRepository.findAll()

  @DeleteMapping("/publisherSubscriptions/{id}")
  @Operation(
    summary = "Delete Publisher Subscription",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "Publisher Subscription deleted",
      ),
    ],
  )
  fun deletePublisherSubscription(
    @Schema(description = "Publisher Subscription ID", required = true)
    @PathVariable
    id: UUID,
  ) = publisherSubscriptionRepository.deleteById(id)
}
