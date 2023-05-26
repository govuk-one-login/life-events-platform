package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.enums.RegExConstants.CLIENT_ID_REGEX
import uk.gov.gdx.datashare.enums.RegExConstants.SQS_QUEUE_NAME_REGEX
import uk.gov.gdx.datashare.enums.RegExConstants.UUID_REGEX
import java.time.LocalDateTime
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Acquirer Subscription response")
data class AcquirerSubscriptionDto(
  @Schema(
    description = "Acquirer subscription Id",
    required = true,
    example = "00000000-0000-0001-0000-000000000000",
    pattern = UUID_REGEX,
  )
  val acquirerSubscriptionId: UUID,
  @Schema(
    description = "Acquirer Id",
    required = true,
    example = "00000000-0000-0001-0000-000000000000",
    pattern = UUID_REGEX,
  )
  val acquirerId: UUID,
  @Schema(
    description = "Client ID used to access event platform",
    required = false,
    example = "an-oauth-client",
    maxLength = 50,
    pattern = CLIENT_ID_REGEX,
  )
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
  @Schema(
    description = "SQS queue name to receive events for this client. If present, events are sent to this queue instead of being made available via the API",
    required = false,
    example = "acq_acquirer-queue",
    maxLength = 80,
    pattern = SQS_QUEUE_NAME_REGEX,
  )
  val queueName: String? = null,
  @Schema(
    description = "Provides the url of the SQS queue when it is created",
    required = false,
    example = "https://sqs.eu-west-2.amazonaws.com/000000000000/acq_example-queue",
  )
  val queueUrl: String? = null,
  @Schema(description = "Indicates when the subscription was created", required = true, example = "2023-01-04T12:30:00")
  val whenCreated: LocalDateTime,
)
