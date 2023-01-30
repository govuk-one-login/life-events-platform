package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.annotation.JsonInclude
import com.toedter.spring.hateoas.jsonapi.JsonApiId
import com.toedter.spring.hateoas.jsonapi.JsonApiMeta
import com.toedter.spring.hateoas.jsonapi.JsonApiTypeForClass
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.gdx.datashare.enums.EventType
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Subscribed event notification")
@JsonApiTypeForClass("events")
data class EventNotification(
  @Schema(description = "Event ID (UUID)", required = true, example = "d8a6f3ba-e915-4e79-8479-f5f5830f4622")
  @JsonApiId
  val eventId: UUID,
  @Schema(
    description = "Event's Type",
    required = true,
    example = "DEATH_NOTIFICATION",
    allowableValues = ["DEATH_NOTIFICATION", "LIFE_EVENT"],
  )
  val eventType: EventType,
  @Schema(description = "ID from the source of the notification", required = true, example = "999999901")
  val sourceId: String,
  @Schema(
    description = "Indicates that event data is returned when true,",
    required = false,
    example = "false",
  )
  val dataIncluded: Boolean? = null,
  @JsonApiMeta
  @Schema(
    description = "List of data fields that will be returned in this event",
    required = false,
    example = "[\"firstNames\", \"lastName\"]",
  )
  val enrichmentFields: List<String>? = null,
  @Schema(
    description = "<h2>Event Data</h2>" +
      "     This field is only populated when the consumer has <em>enrichmentFieldsIncludedInPoll</em> enabled, otherwise an empty object." +
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
      "      <p><b>Mandatory Fields</b>: registrationDate, firstNames, lastName, sex, dateOfDeath</p>",
    required = false,
  )
  val eventData: Any? = null,
)
