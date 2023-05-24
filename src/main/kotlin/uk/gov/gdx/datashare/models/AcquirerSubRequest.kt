package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Length
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.enums.RegExConstants.CLIENT_ID_REGEX
import uk.gov.gdx.datashare.enums.RegExConstants.SQS_QUEUE_NAME_REGEX
import uk.gov.gdx.datashare.models.validators.SingleConsumptionMethod

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Acquirer Subscription Request")
@SingleConsumptionMethod
data class AcquirerSubRequest(
  @Schema(description = "Events Type", required = true, example = "DEATH_NOTIFICATION")
  val eventType: EventType,
  @Schema(
    description = "Client ID used to access event platform",
    required = false,
    example = "an-oauth-client",
    maxLength = 50,
    pattern = CLIENT_ID_REGEX,
  )
  @get:Length(min = 3, max = 50)
  @get:Pattern(
    regexp = CLIENT_ID_REGEX,
    message = "Name may only contain letters, numbers, and the following special characters: _ -",
  )
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
  @Schema(
    description = "SQS queue name to receive events for this client. If present, events are sent to this queue instead of being made available via the API. This queue will be created",
    required = false,
    example = "acq_event-queue",
    maxLength = 80,
  )
  @get:Pattern(
    regexp = SQS_QUEUE_NAME_REGEX,
    message = "Value must be between 1 and 80 characters and contain alphanumeric characters, hyphens and underscores only. It must begin with `acq_`. It may end in `.fifo`",
  )
  val queueName: String? = null,
  @Schema(
    description = "ARN of AWS principal to be granted read access on the queue",
    required = false,
    example = "arn:aws:iam::000000000000:role/role-name",
  )
  val principalArn: String? = null,
)
