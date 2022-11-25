package uk.gov.gdx.datashare.repository

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import java.time.LocalDateTime

data class EventData(
  @Id
  val eventId: String,
  val eventType: String,
  val dataProvider: String,
  val datasetType: String,
  val dataId: String,
  val dataPayload: String?,
  val dataExpiryTime: LocalDateTime,

  val whenCreated: LocalDateTime? = null,

  @Transient
  @Value("false")
  val new: Boolean = true

) : Persistable<String> {

  override fun getId(): String = eventId

  override fun isNew(): Boolean = new
}
