package uk.gov.gdx.datashare.models

import io.swagger.v3.oas.annotations.media.Schema

data class CognitoClientResponse(
  @Schema(description = "Cognito client name", required = true, example = "HMPO")
  val clientName: String,
  @Schema(description = "Cognito client id", required = true, example = "1234abc")
  val clientId: String,
  @Schema(description = "Cognito client secret", required = true, example = "1234abc")
  val clientSecret: String,
)
