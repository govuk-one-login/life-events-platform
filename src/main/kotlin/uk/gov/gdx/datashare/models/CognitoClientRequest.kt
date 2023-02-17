package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Length
import uk.gov.gdx.datashare.enums.CognitoClientType
import uk.gov.gdx.datashare.enums.RegExConstants.CLIENT_NAME_REGEX

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Cognito Client Request")
data class CognitoClientRequest(
  @Schema(
    description = "Name of client, may only contain lowercase letters, numbers, and the following special characters: + = , . @ -",
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
  val clientName: String,
  @ArraySchema(schema = Schema(description = "Client type", required = true, example = "ACQUIRER"))
  val clientTypes: List<CognitoClientType>,
)
