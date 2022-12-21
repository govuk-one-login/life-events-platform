package uk.gov.gdx.datashare.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.gdx.datashare.service.SubscriptionManagerService

@RestController
@RequestMapping("/core-data", produces = [ MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasAnyAuthority('SCOPE_pubsub/maintain')")
@Validated
class PubSubProviderManager(
  private val subscriptionManagerService: SubscriptionManagerService
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
}
