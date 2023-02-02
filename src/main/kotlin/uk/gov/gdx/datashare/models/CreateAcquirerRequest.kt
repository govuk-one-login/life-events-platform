package uk.gov.gdx.datashare.models

import io.swagger.v3.oas.annotations.media.Schema
import org.hibernate.validator.constraints.Length
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType
import javax.validation.constraints.Pattern

class CreateAcquirerRequest(
  @Schema(
    description = "Name of client, may only contain lowercase letters, numbers, and the following special characters: + = , . @ -",
    required = true,
    example = "hmpo",
    maxLength = 128,
    pattern = "^[a-z0-9+=,.@-]*\$",
  )
  @get:Length(min = 3, max = 128)
  @get:Pattern(
    regexp = "^[a-z0-9+=,.@-]*\$",
    message = "Name may only contain lowercase letters, numbers, and the following special characters: + = , . @ -",
  )
  val clientName: String,
  @Schema(description = "Event's Type", required = true, example = "DEATH_NOTIFICATION")
  val eventType: EventType,
  @Schema(
    description = "List of required fields to enrich the event with",
    required = true,
    example = "[\"firstNames\",\"lastName\"]",
  )
  val enrichmentFields: List<EnrichmentField>,
  @Schema(
    description = "Indicates that the specified enrichment fields will be present when a poll of events occurs",
    required = false,
    defaultValue = "false",
    example = "false",
  )
  val enrichmentFieldsIncludedInPoll: Boolean,
)
