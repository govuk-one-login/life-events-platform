package uk.gov.gdx.datashare.repositories

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.enums.RegExConstants.CLIENT_ID_REGEX
import uk.gov.gdx.datashare.enums.RegExConstants.UUID_REGEX
import java.time.LocalDateTime
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Supplier Subscription")
data class SupplierSubscription(
  @Id
  @JvmField
  @Schema(description = "Supplier Subscription ID", required = true, example = "00000000-0000-0001-0000-000000000000", pattern = UUID_REGEX)
  val id: UUID = UUID.randomUUID(),
  @Schema(description = "Supplier ID", required = true, example = "00000000-0000-0001-0000-000000000000", pattern = UUID_REGEX)
  val supplierId: UUID,
  @Schema(description = "Client ID", required = true, example = "a-client-id", maxLength = 50, pattern = CLIENT_ID_REGEX)
  val clientId: String,
  @Schema(description = "Events Type", required = true, example = "DEATH_NOTIFICATION")
  val eventType: EventType,
  @Schema(description = "Indicates when the Supplier subscription was created", required = true, example = "2023-01-04T12:30:00")
  val whenCreated: LocalDateTime = LocalDateTime.now(),
  @Schema(description = "Indicates when the Supplier subscription was deleted", required = false, example = "2023-01-04T12:30:00")
  val whenDeleted: LocalDateTime? = null,

  @Transient
  @Value("false")
  @JsonIgnore
  val new: Boolean = true,

  ) : Persistable<UUID> {
  @JsonIgnore
  override fun getId(): UUID = id

  override fun isNew(): Boolean = new
}
