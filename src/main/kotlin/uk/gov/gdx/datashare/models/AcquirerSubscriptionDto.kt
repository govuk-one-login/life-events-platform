package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.enums.RegExConstants.CLIENT_ID_REGEX
import uk.gov.gdx.datashare.enums.RegExConstants.UUID_REGEX
import java.time.LocalDateTime
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Acquirer Subscription response")
data class AcquirerSubscriptionDto(
  @Schema(description = "Acquirer subscription Id", required = true, example = "00000000-0000-0001-0000-000000000000", pattern = UUID_REGEX)
  val acquirerSubscriptionId: UUID,
  @Schema(description = "Acquirer Id", required = true, example = "00000000-0000-0001-0000-000000000000", pattern = UUID_REGEX)
  val acquirerId: UUID,
  @Schema(description = "Client ID used to access event platform", required = false, example = "an-oauth-client", maxLength = 50, pattern = CLIENT_ID_REGEX)
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
