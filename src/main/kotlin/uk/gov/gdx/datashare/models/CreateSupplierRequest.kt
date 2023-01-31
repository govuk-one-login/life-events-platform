package uk.gov.gdx.datashare.models

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.gdx.datashare.enums.EventType

data class CreateSupplierRequest(
  @Schema(description = "Name of client, must be lower case", required = true, example = "HMPO")
  val clientName: String,
  @Schema(description = "Event's Type", required = true, example = "DEATH_NOTIFICATION")
  val eventType: EventType,
)
