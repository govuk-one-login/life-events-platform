package uk.gov.gdx.datashare.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.flow.toList
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.gdx.datashare.repository.EventDataRepository

@RestController
@RequestMapping("/admin", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasAnyAuthority('SCOPE_events/admin')")
@Validated
@Tag(name = "01. Admin")
class AdminController(
    private val eventDataRepository: EventDataRepository,
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
  suspend fun getEvents() = eventDataRepository.findAll().toList()
}
