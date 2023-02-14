package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Length
import uk.gov.gdx.datashare.enums.EventType

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Create Supplier Request")
data class CreateSupplierRequest(
  @Schema(
    description = "Name of client, may only contain lowercase letters, numbers, and the following special characters: + = , . @ -",
    required = true,
    example = "hmpo",
    maxLength = 128,
    pattern = "^[a-z0-9+=,.@-]*\$",
  )
  @get:Length(min = 3, max = 128)
  @get:Pattern(
    regexp = "^[a-z0-9+=,.@-]*\$",
    message = "Name may only contain lowercase letters, numbers, and the following special characters: + = , . @ -",
  )
  val clientName: String,
  @Schema(description = "Event's Type", required = true, example = "DEATH_NOTIFICATION")
  val eventType: EventType,
)
