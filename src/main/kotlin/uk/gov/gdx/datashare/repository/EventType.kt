package uk.gov.gdx.datashare.repository

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime

data class EventType(

  @Id
  @Column("id")
  val eventId: String,
  val description: String,
  val active: Boolean = true,
  val whenCreated: LocalDateTime? = null,

  @Transient
  @Value("false")
  val new: Boolean = true

) : Persistable<String> {
  override fun getId() = eventId

  override fun isNew(): Boolean = new
}
