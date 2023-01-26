package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.annotation.JsonInclude
import uk.gov.gdx.datashare.enums.EventType
import java.time.LocalDateTime
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DataProcessorMessage(
  val eventType: EventType,
  val eventTime: LocalDateTime,
  val publisher: String,
  val subscriptionId: UUID,
  val id: String,
)
