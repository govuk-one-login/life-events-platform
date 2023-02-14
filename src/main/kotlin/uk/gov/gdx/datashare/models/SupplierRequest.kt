package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Supplier Request")
data class SupplierRequest(
  @Schema(description = "Supplier name", required = true, example = "DWP", maxLength = 80, pattern = "^[a-zA-Z\\d. _-]{1,80}\$")
  val name: String,
)
