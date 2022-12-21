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
data class EventPublisher(
  @Id
  @Column("id")
  @Schema(description = "Publisher ID", required = true, example = "00000000-0000-0001-0000-000000000000")
  val publisherId: UUID = UUID.randomUUID(),
  @Schema(description = "Publisher Name", required = true, example = "HMPO")
  val publisherName: String,
  val whenCreated: LocalDateTime? = null,

  @Transient
  @Value("false")
  @JsonIgnore
  val new: Boolean = true

) : Persistable<UUID> {
  override fun getId(): UUID = publisherId

  override fun isNew(): Boolean = new
}
