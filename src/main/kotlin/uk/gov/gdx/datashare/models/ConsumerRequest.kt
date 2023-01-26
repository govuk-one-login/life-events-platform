package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Consumer Request")
data class ConsumerRequest(
  @Schema(description = "Consumer name", required = true, example = "DWP")
  val name: String,
)
