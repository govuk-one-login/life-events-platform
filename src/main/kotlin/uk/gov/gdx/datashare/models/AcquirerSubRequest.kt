package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Acquirer Subscription Request")
data class AcquirerSubRequest(
  @Schema(description = "Events Type", required = true, example = "DEATH_NOTIFICATION")
  val eventType: EventType,
  @Schema(description = "Client ID used to access event platform", required = false, example = "an-oauth-client", maxLength = 50, pattern = "^[a-zA-Z0-9-_]{1,50}\$")
  val oauthClientId: String? = null,
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
  val enrichmentFieldsIncludedInPoll: Boolean = false,
)
