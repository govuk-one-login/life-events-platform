package uk.gov.gdx.datashare.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.gdx.datashare.service.DeathDataAndMore
import uk.gov.gdx.datashare.service.GdxDataShareService

@RestController
@RequestMapping("/datashare", produces = [ MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasAuthority('SCOPE_event:read')")
class DataShareResource(
  private val dataShareService: GdxDataShareService
) {

  @GetMapping("/{id}")
  @Operation(
    summary = "Retrieves death data from LEV and other services for return to client",
    description = "The event ID is the UUID received off the queue, Need scope of event:read",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Data Returned"
      )
    ]
  )
  suspend fun getDataForEventId(
    @Schema(description = "ID", required = true, type = "String")
    @PathVariable id: String,
  ): DeathDataAndMore = dataShareService.retrieveLevData(id)
}
