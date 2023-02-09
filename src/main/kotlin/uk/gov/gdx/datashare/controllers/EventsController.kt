package uk.gov.gdx.datashare.controllers

import com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API_VALUE
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.links.Link
import io.swagger.v3.oas.annotations.links.LinkParameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.PagedModel
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.gdx.datashare.config.ErrorResponse
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.helpers.getPageLinks
import uk.gov.gdx.datashare.models.EventNotification
import uk.gov.gdx.datashare.models.EventToPublish
import uk.gov.gdx.datashare.services.*
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/events", produces = [JSON_API_VALUE])
class EventsController(
  private val eventDataService: EventDataService,
  private val eventApiAuditService: EventApiAuditService,
  private val dataReceiverService: DataReceiverService,
  private val meterRegistry: MeterRegistry,
) : BaseApiController() {
  companion object {
    private const val DEFAULT_PAGE_SIZE = 100
  }

  @Tag(name = "01. Acquirer")
  @PreAuthorize("hasAnyAuthority('SCOPE_events/consume')")
  @GetMapping
  @Operation(
    operationId = "getEvents",
    summary = "Event Get API - Get event data",
    description = "Get all events for Acquirer, Need scope of events/consume. " +
      "     This field is only populated when the acquirer has <em>enrichmentFieldsIncludedInPoll</em> enabled, otherwise an empty object." +
      "     Full dataset for the event can be obtained by calling <pre>/events/{id}</pre>" +
      "     <h3>Event Types</h3>" +
      "     <h4>1. Death Notification - Type: <em>DEATH_NOTIFICATION</em></h4>" +
      "     <p>Death notifications take the following json structure." +
      "     <pre>\n" +
      "{\n" +
      "        \"registrationDate\": \"2023-01-23\",\n" +
      "        \"firstNames\": \"Mary Jane\",\n" +
      "        \"lastName\": \"Smith\",\n" +
      "        \"maidenName\": \"Jones\",\n" +
      "        \"sex\": \"Male\",\n" +
      "        \"dateOfDeath\": \"2023-01-02\",\n" +
      "        \"dateOfBirth\": \"1972-02-20\",\n" +
      "        \"birthPlace\": \"56 Test Address, B Town\",\n" +
      "        \"deathPlace\": \"Hospital Ward 5, C Town\",\n" +
      "        \"occupation\": \"Doctor\",\n" +
      "        \"retired\": true,\n" +
      "        \"address\": \"101 Address Street, A Town, Postcode\"\n" +
      "}\n" +
      "      </pre>" +
      "      <p><b>Mandatory Fields</b>: registrationDate, firstNames, lastName, sex, dateOfDeath</p>" +
      "      <p><b>Gender Types:</b>: Male, Female, Indeterminate</p>" +
      "<h4>2. Person has been sent to prison - Type: <em>ENTERED_PRISON</em></h4>" +
      "     <p>Prisoner received notifications take the following json structure." +
      "     <pre>\n" +
      "{\n" +
      "        \"prisonerNumber\": \"A1234DB\",\n" +
      "        \"firstName\": \"Mary\",\n" +
      "        \"middleNames\": \"Jane\",\n" +
      "        \"lastName\": \"Smith\",\n" +
      "        \"gender\": \"Male\",\n" +
      "        \"dateOfBirth\": \"1972-02-20\",\n" +
      "}\n" +
      "      </pre>" +
      "      <p><b>Mandatory Fields</b>: prisonerNumber, firstName, lastName, gender, dateOfBirth</p>",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Events",
        links = [
          Link(
            name = "Get event details",
            operationId = "getEvent",
            parameters = [LinkParameter(name = "id", expression = "\$response.body#/content/0/eventId")],
            description = "Full details of the event",
          ),
        ],
        content = arrayOf(
          Content(
            examples = arrayOf(
              ExampleObject(
                value = """
{
  "data": [
    {
      "id": "a3e48cca-052f-4599-8ddc-e863de428f89",
      "type": "events",
      "attributes": {
        "eventType": "DEATH_NOTIFICATION",
        "sourceId": "123456789",
        "eventData": {
          "firstNames": "Joan Narcissus Ouroboros",
          "lastName": "SMITH",
          "sex": "Male",
          "dateOfDeath": "2023-01-20"
        }
      },
      "links": {
        "self": "http://localhost:8080/events/a3e48cca-052f-4599-8ddc-e863de428f89"
      }
    },
    {
      "id": "184ae4c3-17c5-41b8-a1f2-0abefecdb6ca",
      "type": "events",
      "attributes": {
        "eventType": "DEATH_NOTIFICATION",
        "sourceId": "123456789",
        "eventData": {
          "firstNames": "Joan Narcissus Ouroboros",
          "lastName": "SMITH",
          "sex": "Male",
          "dateOfDeath": "2023-01-20"
        }
      },
      "links": {
        "self": "http://localhost:8080/events/184ae4c3-17c5-41b8-a1f2-0abefecdb6ca"
      }
    }
  ],
  "links": {
    "self": "http://localhost:8080/events?page%5Bnumber%5D=1&page%5Bsize%5D=2",
    "first": "http://localhost:8080/events?page%5Bsize%5D=2&page%5Bnumber%5D=0",
    "prev": "http://localhost:8080/events?page%5Bsize%5D=2&page%5Bnumber%5D=0",
    "next": "http://localhost:8080/events?page%5Bsize%5D=2&page%5Bnumber%5D=2",
    "last": "http://localhost:8080/events?page%5Bsize%5D=2&page%5Bnumber%5D=5"
  },
  "meta": {
    "page": {
      "size": 2,
      "totalElements": 11,
      "totalPages": 6,
      "number": 1
    }
  }
}
              """,
              ),
            ),
          ),
        ),
      ),
    ],
  )
  fun getEvents(
    @Schema(
      description = "Event Types, if none supplied it will be the allowed types for this client",
      required = false,
    )
    @RequestParam(name = "filter[eventType]", required = false)
    eventTypes: List<EventType>? = null,
    @Schema(
      description = "Events after this time, if not supplied it will be from the last time this endpoint was called for this client",
      type = "string",
      format = "date-time",
      required = false,
    )
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @RequestParam(name = "filter[fromTime]", required = false)
    startTime: LocalDateTime? = null,
    @Schema(
      description = "Events before this time, if not supplied it will be now",
      type = "string",
      format = "date-time",
      required = false,
    )
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @RequestParam(name = "filter[toTime]", required = false)
    endTime: LocalDateTime? = null,
    @Schema(
      description = "Page number. Zero indexed.",
      defaultValue = "0",
      minimum = "0",
    )
    @RequestParam(name = "page[number]", defaultValue = "0")
    @PositiveOrZero
    pageNumber: Int,
    @Schema(
      description = "Number of items per page.",
      defaultValue = DEFAULT_PAGE_SIZE.toString(),
      minimum = "0",
    )
    @RequestParam(name = "page[size]", defaultValue = "100")
    @Positive
    pageSize: Int,
  ): PagedModel<EntityModel<EventNotification>> = run {
    tryCallAndUpdateMetric(
      { buildEventsResponse(eventTypes, startTime, endTime, pageSize, pageNumber) },
      meterRegistry.counter("API_CALLS.GetEvents", "success", "true"),
      meterRegistry.counter("API_CALLS.GetEvents", "success", "false"),
    )
  }

  private fun buildEventsResponse(
    eventTypes: List<EventType>?,
    startTime: LocalDateTime?,
    endTime: LocalDateTime?,
    pageSize: Int,
    pageNumber: Int,
  ): PagedModel<EntityModel<EventNotification>> {
    val events = eventDataService.getEvents(eventTypes, startTime, endTime, pageNumber, pageSize)
    eventApiAuditService.auditEventApiCall(events.eventModels)
    val linkBuilder =
      eventsLink(eventTypes, startTime, endTime, pageNumber, pageSize).toUriComponentsBuilder().scheme("https")
    val pageMetadata = PagedModel.PageMetadata(pageSize.toLong(), pageNumber.toLong(), events.count.toLong())
    val selfLink = org.springframework.hateoas.Link.of(linkBuilder.build().toUriString(), "self")
    val pageLinks = getPageLinks(pageMetadata, linkBuilder)
    val links = arrayListOf(selfLink) + pageLinks
    return PagedModel.of(
      events.eventModels.map { EntityModel.of(it, eventLink(it.eventId)) },
      pageMetadata,
      links,
    )
  }

  @Tag(name = "01. Acquirer")
  @PreAuthorize("hasAnyAuthority('SCOPE_events/consume')")
  @GetMapping("/{id}")
  @Operation(
    summary = "Get Specific Event API - Get event data",
    description = "The event ID is the UUID received off the queue, Need scope of events/consume",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Event",
        content = arrayOf(
          Content(
            examples = arrayOf(
              ExampleObject(
                value = """
{
  "data": {
    "id": "a3e48cca-052f-4599-8ddc-e863de428f89",
    "type": "events",
    "attributes": {
      "eventType": "DEATH_NOTIFICATION",
      "sourceId": "123456789",
      "eventData": {
        "firstNames": "Joan Narcissus Ouroboros",
        "lastName": "SMITH",
        "sex": "Male",
        "dateOfDeath": "2023-01-20"
      }
    }
  },
  "links": {
    "self": "http://localhost:8080/events/a3e48cca-052f-4599-8ddc-e863de428f89",
    "collection": "http://localhost:8080/events?page%5Bnumber%5D=0&page%5Bsize%5D=10"
  }
}
              """,
              ),
            ),
          ),
        ),
      ),
      ApiResponse(
        responseCode = "404",
        description = "Event with UUID cannot be found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getEvent(
    @Schema(description = "Event ID", required = true)
    @PathVariable
    id: UUID,
  ): EntityModel<EventNotification>? = run {
    tryCallAndUpdateMetric(
      {
        eventDataService.getEvent(id).let {
          eventApiAuditService.auditEventApiCall(it)
          EntityModel.of(
            it,
            eventLink(id),
            org.springframework.hateoas.Link.of(
              eventsLink().toUriComponentsBuilder().scheme("https").build().toUriString(),
              "collection",
            ),
          )
        }
      },
      meterRegistry.counter("API_CALLS.GetEvent", "success", "true"),
      meterRegistry.counter("API_CALLS.GetEvent", "success", "false"),
    )
  }

  @Tag(name = "01. Acquirer")
  @PreAuthorize("hasAnyAuthority('SCOPE_events/consume')")
  @DeleteMapping("/{id}")
  @Operation(
    summary = "Event Delete API - Delete event data",
    description = "The event ID is the UUID received off the queue, Need scope of events/consume",
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
  ): ResponseEntity<Void> {
    try {
      val eventNotification = eventDataService.deleteEvent(id)
      eventApiAuditService.auditEventApiCall(eventNotification)
      meterRegistry.counter("API_CALLS.DeleteEvent", "success", "true").increment()
      return ResponseEntity<Void>(HttpStatus.NO_CONTENT)
    } catch (e: Exception) {
      meterRegistry.counter("API_CALLS.DeleteEvent", "success", "false").increment()
      throw e
    }
  }

  @Tag(name = "02. Supplier")
  @PreAuthorize("hasAnyAuthority('SCOPE_events/publish')")
  @PostMapping
  @Operation(
    summary = "Send events to GDS - The 'Source' of the event - this could be HMPO or DWP for example",
    description = "Scope is events/publish",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Data Accepted",
      ),
    ],
  )
  fun publishEvent(
    @Schema(
      description = "Event Payload",
      required = true,
      implementation = EventToPublish::class,
    )
    @RequestBody
    eventPayload: EventToPublish,
  ) = run {
    tryCallAndUpdateMetric(
      { dataReceiverService.sendToDataProcessor(eventPayload) },
      meterRegistry.counter("API_CALLS.PublishEvent", "success", "true"),
      meterRegistry.counter("API_CALLS.PublishEvent", "success", "false"),
    )
  }

  private fun eventLink(id: UUID): org.springframework.hateoas.Link =
    org.springframework.hateoas.Link.of(
      linkTo(methodOn(EventsController::class.java).getEvent(id) as Any).toUriComponentsBuilder().scheme("https")
        .build().toUriString(),
      "self",
    )

  private fun eventsLink(
    eventTypes: List<EventType>? = null,
    startTime: LocalDateTime? = null,
    endTime: LocalDateTime? = null,
    pageNumber: Int = 0,
    pageSize: Int = DEFAULT_PAGE_SIZE,
  ): WebMvcLinkBuilder =
    linkTo(methodOn(EventsController::class.java).getEvents(eventTypes, startTime, endTime, pageNumber, pageSize))

  private fun <T> tryCallAndUpdateMetric(
    call: () -> T,
    successCounter: Counter,
    failureCounter: Counter,
  ): T {
    try {
      val result = call()
      successCounter.increment()
      return result
    } catch (e: Exception) {
      failureCounter.increment()
      throw e
    }
  }
}
