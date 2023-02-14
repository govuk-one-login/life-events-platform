package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Length
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Create Acquirer Request")
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
  @ArraySchema(
    schema = Schema(
      description = "Field to enrich the event with",
      required = true,
      example = "firstNames",
    ),
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
