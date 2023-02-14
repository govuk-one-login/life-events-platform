package uk.gov.gdx.datashare.repositories

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Event Data")
data class EventData(
  @Id
  @Column("id")
  @Schema(description = "Event ID", required = true, example = "00000000-0000-0001-0000-000000000000", pattern = "^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}\$")
  val eventId: UUID = UUID.randomUUID(),
  @Schema(description = "Acquirer subscription ID", required = true, example = "00000000-0000-0001-0000-000000000000", pattern = "^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}\$")
  val acquirerSubscriptionId: UUID,
  @Schema(description = "Event data specific ID", required = true, example = "HMPO Death certificate number", maxLength = 80, pattern = "^[A-Za-z0-9+=,.@-_]*\$")
  val dataId: String,
  val whenCreated: LocalDateTime = LocalDateTime.now(),
  val eventTime: LocalDateTime = LocalDateTime.now(),

  @Transient
  @Value("false")
  val new: Boolean = true,

  @Schema(description = "When event deleted", required = false)
  val deletedAt: LocalDateTime? = null,

) : Persistable<UUID> {
  @JsonIgnore
  override fun getId() = eventId

  override fun isNew(): Boolean = new
}
