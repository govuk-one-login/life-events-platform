package uk.gov.gdx.datashare.repository

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime
import java.util.*

data class EgressEventType(
  @Id
  @Column("id")
  val eventTypeId: UUID,
  @Schema(description = "Events Type of Ingress Notification", required = true, example = "DEATH_NOTIFICATION")
  val ingressEventType: String,
  @Schema(description = "Description", required = true, example = "Enriched Death Notification for DWP")
  val description: String,
  @Schema(description = "CSV List of required fields to enrich the event with", required = true, example = "firstName,lastName")
  val enrichmentFields: String,
  val active: Boolean = true,
  val whenCreated: LocalDateTime? = null,

  @Transient
  @Value("false")
  val new: Boolean = true

) : Persistable<UUID> {
  override fun getId() = eventTypeId

  override fun isNew(): Boolean = new
}