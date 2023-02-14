package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Cognito Client Response")
data class CognitoClientResponse(
  @Schema(description = "Cognito client name", required = true, example = "HMPO", maxLength = 80, pattern = "^[a-zA-Z0-9-_]{50}\$")
  val clientName: String,
  @Schema(description = "Cognito client id", required = true, example = "1234abc", maxLength = 50, pattern = "^[a-zA-Z0-9-_]{50}\$")
  val clientId: String,
  @Schema(description = "Cognito client secret", required = true, example = "1234abc", maxLength = 80, pattern = "^[a-zA-Z0-9]{80}\$")
  val clientSecret: String,
)
