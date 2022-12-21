package uk.gov.gdx.datashare.repository

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime
import java.util.*

data class EventData(
  @Id
  @Column("id")
  val eventId: UUID,
  val eventTypeId: String,
  val datasetId: String,
  val subscriptionId: UUID,
  val dataId: String,
  val dataPayload: String?,
  val dataExpiryTime: LocalDateTime,
  val whenCreated: LocalDateTime? = null,

  @Transient
  @Value("false")
  val new: Boolean = true

) : Persistable<UUID> {

  override fun getId() = eventId

  override fun isNew(): Boolean = new
}
