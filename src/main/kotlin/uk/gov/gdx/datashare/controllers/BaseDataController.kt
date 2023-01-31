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
  private val publisherRepository: PublisherRepository,
  private val publisherSubscriptionRepository: PublisherSubscriptionRepository,
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
