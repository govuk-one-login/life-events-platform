package uk.gov.gdx.datashare.repositories

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import java.time.LocalDateTime
import java.util.*

data class AcquirerEvent(
  @Id
  @JvmField
  val id: UUID = UUID.randomUUID(),
  val supplierEventId: UUID,
  val acquirerSubscriptionId: UUID,
  val dataId: String,
  val eventTime: LocalDateTime?,
  val createdAt: LocalDateTime = LocalDateTime.now(),
  var deletedAt: LocalDateTime? = null,

  @Transient
  @Value("false")
  val new: Boolean = true,

  ) : Persistable<UUID> {

  @JsonIgnore
  override fun getId() = id

  override fun isNew(): Boolean = new
}
