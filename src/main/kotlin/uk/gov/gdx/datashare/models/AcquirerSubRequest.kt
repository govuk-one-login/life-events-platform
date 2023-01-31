package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.gdx.datashare.enums.EventType

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Acquirer Subscription Request")
data class AcquirerSubRequest(
  @Schema(description = "Events Type", required = true, example = "DEATH_NOTIFICATION")
  val eventType: EventType,
  @Schema(description = "Client ID used to access event platform", required = false, example = "an-oauth-client")
  val oauthClientId: String? = null,
  @Schema(
    description = "List of required fields to enrich the event with",
    required = true,
    example = "[\"firstNames\", \"lastName\"]",
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
