package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.gdx.datashare.enums.CognitoClientType

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Cognito Client Request")
data class CognitoClientRequest(
  @Schema(
    description = "Name of client, may only contain lowercase letters, numbers, and the following special characters: + = , . @ -",
    required = true,
    example = "hmpo",
    maxLength = 128,
    pattern = "^[a-z0-9+=,.@-]*\$",
  )
  val clientName: String,
  @ArraySchema(schema = Schema(description = "Client type", required = true, example = "ACQUIRER"))
  val clientTypes: List<CognitoClientType>,
)
