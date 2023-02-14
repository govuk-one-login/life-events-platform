package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType
import java.time.LocalDateTime
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Acquirer Subscription response")
data class AcquirerSubscriptionDto(
  @Schema(description = "Acquirer subscription Id", required = true, example = "00000000-0000-0001-0000-000000000000", pattern = "^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}\$")
  val acquirerSubscriptionId: UUID,
  @Schema(description = "Acquirer Id", required = true, example = "00000000-0000-0001-0000-000000000000", pattern = "^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}\$")
  val acquirerId: UUID,
  @Schema(description = "Client ID used to access event platform", required = false, example = "an-oauth-client", maxLength = 50, pattern = "^[a-zA-Z0-9-_]{1,50}\$")
  val oauthClientId: String? = null,
  @Schema(description = "Events Type", required = true, example = "DEATH_NOTIFICATION")
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
  val whenCreated: LocalDateTime,
)
