package uk.gov.gdx.datashare.repository

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import java.time.LocalDateTime

data class ConsumerSubscription(
  @Id
  val id: Long,
  val consumerId: Long,
  val eventTypeId: String,
  val pollClientId: String?,
  val callbackClientId: String?,
  val lastPollEventTime: LocalDateTime? = null,
  val pushUri: String?,
  val ninoRequired: Boolean = false,
  val whenCreated: LocalDateTime? = null,

  @Transient
  @Value("false")
  val new: Boolean = true

) : Persistable<String> {

  override fun getId(): String = id.toString()

  override fun isNew(): Boolean = new
}
