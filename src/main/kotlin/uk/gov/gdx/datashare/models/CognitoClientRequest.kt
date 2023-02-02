package uk.gov.gdx.datashare.models

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.gdx.datashare.enums.CognitoClientType

data class CognitoClientRequest(
  @Schema(description = "Name of client", required = true, example = "HMPO")
  val clientName: String,
  @ArraySchema(schema = Schema(description = "Client type", required = true, example = "ACQUIRER"))
  val clientTypes: List<CognitoClientType>,
)
