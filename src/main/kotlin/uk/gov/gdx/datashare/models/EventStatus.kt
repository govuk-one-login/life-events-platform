package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.gdx.datashare.enums.EventType

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Event type count")
data class EventStatus(
    @Schema(
    description = "Event's Type",
    required = true,
    example = "DEATH_NOTIFICATION",
  )
  val eventType: EventType,
    @Schema(description = "Number of events for the type", required = true, example = "123")
  val count: Number,
)
