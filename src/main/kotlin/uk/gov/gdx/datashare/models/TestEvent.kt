package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Test event")
data class TestEvent(
  @Schema(description = "Test field", required = true, example = "1234")
  val testField: String? = null,
)
