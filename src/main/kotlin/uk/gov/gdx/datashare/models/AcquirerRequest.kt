package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.gdx.datashare.enums.RegExConstants.SUP_ACQ_NAME_REGEX

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Acquirer Request")
data class AcquirerRequest(
  @Schema(description = "Acquirer name", required = true, example = "DWP", maxLength = 80, pattern = SUP_ACQ_NAME_REGEX)
  val name: String,
)
