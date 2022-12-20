package uk.gov.gdx.datashare.repository

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import java.time.LocalDateTime

data class EventConsumer(
  @Id
  @Schema(description = "Consumer ID", required = true, example = "1")
  val id: Long,
  @Schema(description = "Consumer Name", required = true, example = "DVLA")
  val consumerName: String,
  val whenCreated: LocalDateTime? = null,

  @Transient
  @Value("false")
  val new: Boolean = true

) : Persistable<Long> {

  override fun getId(): Long = id

  override fun isNew(): Boolean = new
}
