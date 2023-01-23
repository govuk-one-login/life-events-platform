package uk.gov.gdx.datashare.repository

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
data class Consumer(
  @Id
  @Column("id")
  @Schema(description = "Consumer ID", required = true, example = "00000000-0000-0001-0000-000000000000")
  val consumerId: UUID = UUID.randomUUID(),
  @Schema(description = "Consumer Name", required = true, example = "DVLA")
  val name: String,
  val whenCreated: LocalDateTime = LocalDateTime.now(),

  @Transient
  @Value("false")
  @JsonIgnore
  val new: Boolean = true,

) : Persistable<UUID> {

  override fun getId(): UUID = consumerId

  override fun isNew(): Boolean = new
}
