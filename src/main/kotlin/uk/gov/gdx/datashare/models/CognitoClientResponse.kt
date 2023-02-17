package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.gdx.datashare.enums.RegExConstants.CLIENT_ID_REGEX
import uk.gov.gdx.datashare.enums.RegExConstants.CLIENT_NAME_REGEX
import uk.gov.gdx.datashare.enums.RegExConstants.CLIENT_SECRET_REGEX

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Cognito Client Response")
data class CognitoClientResponse(
  @Schema(description = "Cognito client name", required = true, example = "HMPO", maxLength = 80, pattern = CLIENT_NAME_REGEX)
  val clientName: String,
  @Schema(description = "Cognito client id", required = true, example = "1234abc", maxLength = 50, pattern = CLIENT_ID_REGEX)
  val clientId: String,
  @Schema(description = "Cognito client secret", required = true, example = "1234abc", maxLength = 80, pattern = CLIENT_SECRET_REGEX)
  val clientSecret: String,
)
