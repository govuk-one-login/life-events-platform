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
@Schema(description = "Supplier")

data class Supplier(
  @Id
  @Column("id")
  @Schema(description = "Supplier ID", required = true, example = "00000000-0000-0001-0000-000000000000", pattern = "^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}\$")
  val supplierId: UUID = UUID.randomUUID(),
  @Schema(description = "Supplier Name", required = true, example = "HMPO", maxLength = 80, pattern = "^[a-zA-Z\\d. _-]{1,80}\$")
  val name: String,
  val whenCreated: LocalDateTime = LocalDateTime.now(),

  @Transient
  @Value("false")
  @JsonIgnore
  val new: Boolean = true,

) : Persistable<UUID> {
  @JsonIgnore
  override fun getId(): UUID = supplierId

  override fun isNew(): Boolean = new
}
