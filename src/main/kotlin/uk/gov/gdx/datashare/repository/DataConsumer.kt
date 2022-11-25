package uk.gov.gdx.datashare.repository

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import java.time.LocalDateTime

data class DataConsumer(
  @Id
  val clientId: String,
  val clientName: String,
  val allowedEventTypes: String,
  val ninoRequired: Boolean = false,
  val otherDataSets: Boolean = false,
  val whenCreated: LocalDateTime? = null,

  @Transient
  @Value("false")
  val new: Boolean = true

) : Persistable<String> {

  override fun getId(): String = clientId

  override fun isNew(): Boolean = new
}

