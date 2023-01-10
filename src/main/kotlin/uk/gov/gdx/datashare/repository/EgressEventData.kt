package uk.gov.gdx.datashare.repository

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime
import java.util.*

data class EgressEventData(
  @Id
  @Column("id")
  val eventId: UUID = UUID.randomUUID(),
  @Schema(description = "Consumer subscription ID", required = true, example = "00000000-0000-0001-0000-000000000000")
  val consumerSubscriptionId: UUID,
  @Schema(
    description = "Ingress event ID for event that created this egress event",
    required = true,
    example = "00000000-0000-0001-0000-000000000000"
  )
  val ingressEventId: UUID,
  @Schema(description = "Dataset ID", required = true, example = "00000000-0000-0001-0000-000000000000")
  val datasetId: String,
  @Schema(description = "Event data specific ID", required = true, example = "HMPO Death certificate number")
  val dataId: String,
  @Schema(description = "Event data", required = false, example = "{\"firstName\": \"Bob\",...}}")
  val dataPayload: String?,
  val whenCreated: LocalDateTime? = null,
  val eventTime: LocalDateTime? = null,

  @Transient
  @Value("false")
  val new: Boolean = true

) : Persistable<UUID> {

  override fun getId() = eventId

  override fun isNew(): Boolean = new
}
