package uk.gov.gdx.datashare.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import uk.gov.gdx.datashare.repository.*
import java.util.*

@RestController
@RequestMapping("/data", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasAnyAuthority('SCOPE_events/admin')")
@Validated
@Tag(name = "10. Data")
class BaseDataController(
  private val consumerRepository: ConsumerRepository,
  private val consumerSubscriptionRepository: ConsumerSubscriptionRepository,
  private val eventDataRepository: EventDataRepository,
  private val eventDatasetRepository: EventDatasetRepository,
  private val eventTypeRepository: EventTypeRepository,
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
  suspend fun getConsumers() = consumerRepository.findAll()

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
  suspend fun deleteConsumer(
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
  suspend fun getConsumerSubscriptions() = consumerSubscriptionRepository.findAll()

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
  suspend fun deleteConsumerSubscription(
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
  suspend fun getEvents() = eventDataRepository.findAll()

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
  suspend fun deleteEvent(
    @Schema(description = "Event ID", required = true)
    @PathVariable
    id: UUID,
  ) = eventDataRepository.deleteById(id)

  @GetMapping("/eventDatasets")
  @Operation(
    summary = "Get Event Datasets",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Event Dataset",
      ),
    ],
  )
  suspend fun getEventDatasets() = eventDatasetRepository.findAll()

  @DeleteMapping("/eventDatasets/{id}")
  @Operation(
    summary = "Delete Event Dataset",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "Event Dataset deleted",
      ),
    ],
  )
  suspend fun deleteEventDataset(
    @Schema(description = "Event Dataset ID", required = true)
    @PathVariable
    id: String,
  ) = eventDatasetRepository.deleteById(id)

  @GetMapping("/eventTypes")
  @Operation(
    summary = "Get Event Types",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Event Type",
      ),
    ],
  )
  suspend fun getEventTypes() = eventTypeRepository.findAll()

  @DeleteMapping("/eventTypes/{id}")
  @Operation(
    summary = "Delete Event Type",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "Event Type deleted",
      ),
    ],
  )
  suspend fun deleteEventType(
    @Schema(description = "Type ID", required = true)
    @PathVariable
    id: String,
  ) = eventTypeRepository.deleteById(id)

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
  suspend fun getPublishers() = publisherRepository.findAll()

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
  suspend fun deletePublisher(
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
  suspend fun getPublisherSubscriptions() = publisherSubscriptionRepository.findAll()

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
  suspend fun deletePublisherSubscription(
    @Schema(description = "Publisher Subscription ID", required = true)
    @PathVariable
    id: UUID,
  ) = publisherSubscriptionRepository.deleteById(id)

  // The below endpoints don't exist through any other means, so have added them here
  @PostMapping("/eventTypes")
  @Operation(
    summary = "Add Event type type",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Event type added",
      ),
    ],
  )
  suspend fun addEventType(
    @Schema(
      description = "Event type",
      required = true,
      implementation = EventType::class,
    )
    @RequestBody
    eventType: EventType,
  ) = eventTypeRepository.save(eventType)

  @PostMapping("/eventDataset")
  @Operation(
    summary = "Add Event dataset",
    description = "Need scope of events/admin",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Event dataset added",
      ),
    ],
  )
  suspend fun addEventDataset(
    @Schema(
      description = "Event dataset type",
      required = true,
      implementation = EventDataset::class,
    )
    @RequestBody
    eventDataset: EventDataset,
  ) = eventDatasetRepository.save(eventDataset)
}
