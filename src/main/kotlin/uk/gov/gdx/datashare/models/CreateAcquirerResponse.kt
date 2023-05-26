package uk.gov.gdx.datashare.models

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.gdx.datashare.enums.RegExConstants

@Schema(description = "Create Acquirer Response")
class CreateAcquirerResponse(
  @Schema(
    description = "URL of SQS queue that events will be sent to. Either the oauth client details or the queue url will be present, but not both",
    required = false,
    example = "https://sqs.eu-west-2.amazonaws.com/000000000000/acq_example-queue",
  )
  val queueUrl: String?,
  @Schema(
    description = "Cognito client name",
    required = true,
    example = "hmpo",
    maxLength = 80,
    pattern = RegExConstants.CLIENT_NAME_REGEX,
  )
  val clientName: String?,
  @Schema(
    description = "Cognito client id",
    required = true,
    example = "1234abc",
    maxLength = 50,
    pattern = RegExConstants.CLIENT_ID_REGEX,
  )
  val clientId: String?,
  @Schema(
    description = "Cognito client secret",
    required = true,
    example = "1234abc",
    maxLength = 80,
    pattern = RegExConstants.CLIENT_SECRET_REGEX,
  )
  val clientSecret: String?,
)
