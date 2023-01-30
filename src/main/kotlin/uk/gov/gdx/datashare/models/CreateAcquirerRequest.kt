package uk.gov.gdx.datashare.models

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.gdx.datashare.enums.EventType

data class CreateAcquirerRequest(
  @Schema(description = "Name of client", required = true, example = "HMPO")
  val clientName: String,
  @Schema(description = "Event's Type", required = true, example = "DEATH_NOTIFICATION")
  val eventType: EventType,
  @Schema(
    description = "List of required fields to enrich the event with",
    required = true,
    example = "[\"firstNames\",\"lastName\"]",
  )
  val enrichmentFields: List<String>,
  @Schema(
    description = "Indicates that the specified enrichment fields will be present when a poll of events occurs",
    required = false,
    defaultValue = "false",
    example = "false",
  )
  val enrichmentFieldsIncludedInPoll: Boolean = false,
)
