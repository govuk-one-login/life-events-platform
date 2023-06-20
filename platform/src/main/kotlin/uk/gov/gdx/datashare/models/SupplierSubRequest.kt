package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Length
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.enums.RegExConstants.CLIENT_ID_REGEX

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Supplier Subscription Request")
data class SupplierSubRequest(
  @Schema(description = "Client ID", required = true, example = "a-client-id", maxLength = 50, pattern = CLIENT_ID_REGEX)
  @get:Length(min = 3, max = 50)
  @get:Pattern(
    regexp = CLIENT_ID_REGEX,
    message = "Name may only contain letters, numbers, and the following special characters: . -",
  )
  val clientId: String,
  @Schema(description = "Events Type", required = true, example = "DEATH_NOTIFICATION")
  val eventType: EventType,
)
