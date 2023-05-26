package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Length
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.enums.RegExConstants
import uk.gov.gdx.datashare.enums.RegExConstants.CLIENT_NAME_REGEX
import uk.gov.gdx.datashare.models.validators.ValidQueueDetails

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Create Acquirer Request")
@ValidQueueDetails
class CreateAcquirerRequest(
  @Schema(
    description = "Name of acquirer, may only contain lowercase letters, numbers, and the following special characters: + = , . @ -",
    required = true,
    example = "hmpo",
    maxLength = 80,
    pattern = CLIENT_NAME_REGEX,
  )
  @get:Length(min = 3, max = 80)
  @get:Pattern(
    regexp = CLIENT_NAME_REGEX,
    message = "Name may only contain lowercase letters, numbers, and the following special characters: + = , . @ -",
  )
  val acquirerName: String,
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
  val enrichmentFieldsIncludedInPoll: Boolean = false,
  @Schema(
    description = "Name of the SQS queue to be created. If present the queue will be created and no API access will be provided. If null, a cognito client will be created for API access and details returned",
    required = false,
    maxLength = 80,
  )
  @get:Pattern(
    regexp = RegExConstants.SQS_QUEUE_NAME_REGEX,
    message = "Value must be between 1 and 80 characters and contain alphanumeric characters, hyphens and underscores only. It must begin with `acq_`. It may end in `.fifo`",
  )
  val queueName: String? = null,
  @Schema(
    description = "AWS princiapl to grant access to the SQS queue. Required if queue name is present. Must only be present if queue name is present.",
    required = false,
    example = "arn:aws:iam::000000000000:role/example-role",
  )
  val principalArn: String? = null,
)
