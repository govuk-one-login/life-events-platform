package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.enums.RegExConstants.CLIENT_ID_REGEX

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Supplier Subscription Request")
data class SupplierSubRequest(
  @Schema(description = "Client ID", required = true, example = "a-client-id", maxLength = 50, pattern = CLIENT_ID_REGEX)
  val clientId: String,
  @Schema(description = "Events Type", required = true, example = "DEATH_NOTIFICATION")
  val eventType: EventType,
)
