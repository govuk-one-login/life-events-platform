package uk.gov.gdx.datashare.repository

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime

data class EventType(

  @Id
  @Column("id")
  val eventTypeId: String,
  val description: String,
  @Schema(description = "CSV List of fields that can be enriched", required = true, example = "firstName,lastName")
  val fields: String,
  val active: Boolean = true,
  val whenCreated: LocalDateTime = LocalDateTime.now(),

  @Transient
  @Value("false")
  val new: Boolean = true,

) : Persistable<String> {
  override fun getId() = eventTypeId

  override fun isNew(): Boolean = new
}
