package uk.gov.gdx.datashare.repositories

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import java.time.LocalDateTime
import java.util.*

data class SupplierEvent(
  @Id
  @JvmField
  val id: UUID = UUID.randomUUID(),
  val supplierSubscriptionId: UUID,
  val dataId: String,
  val eventTime: LocalDateTime?,
  val createdAt: LocalDateTime = LocalDateTime.now(),

  @Transient
  @Value("false")
  val new: Boolean = true,
): Persistable<UUID> {
  override fun getId() = id
  override fun isNew(): Boolean = new
}
