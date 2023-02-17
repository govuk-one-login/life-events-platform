package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Length
import uk.gov.gdx.datashare.enums.RegExConstants.SUP_ACQ_NAME_REGEX

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Acquirer Request")
data class AcquirerRequest(
  @Schema(description = "Acquirer name", required = true, example = "DWP", minLength = 3, maxLength = 80, pattern = SUP_ACQ_NAME_REGEX)
  @get:Length(min = 3, max = 80)
  @get:Pattern(
    regexp = SUP_ACQ_NAME_REGEX,
    message = "Name may only contain letters, numbers, and the following special characters: . -",
  )
  val name: String,
)
