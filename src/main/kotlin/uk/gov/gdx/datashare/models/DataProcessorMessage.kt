package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.gdx.datashare.enums.EventType
import java.time.LocalDateTime
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Data Processing Message")
data class DataProcessorMessage(
  val eventType: EventType,
  val eventTime: LocalDateTime,
  val supplier: String,
  val subscriptionId: UUID,
  val id: String,
)
