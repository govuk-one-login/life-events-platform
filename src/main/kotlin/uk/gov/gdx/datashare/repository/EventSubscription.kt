package uk.gov.gdx.datashare.repository

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import java.time.LocalDateTime

data class EventSubscription(
  @Id
  val id: Long,
  val publisherId: Long,
  val clientId: String,
  val eventTypeId: String,
  val datasetId: String,
  val whenCreated: LocalDateTime? = null,

  @Transient
  @Value("false")
  val new: Boolean = true

) : Persistable<String> {

  override fun getId(): String = id.toString()

  override fun isNew(): Boolean = new
}
