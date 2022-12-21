package uk.gov.gdx.datashare.repository

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class EventSubscription(
  @Id
  val id: Long,
  @Schema(description = "Publisher ID", required = true, example = "1")
  val publisherId: Long,
  @Schema(description = "Client ID", required = true, example = "a-client-id")
  val clientId: String,
  @Schema(description = "Events Type", required = true, example = "DEATH_NOTIFICATION")
  val eventTypeId: String,
  @Schema(description = "Data Set", required = true, example = "DEATH_LEN")
  val datasetId: String,
  val whenCreated: LocalDateTime? = null,

  @Transient
  @Value("false")
  @JsonIgnore
  val new: Boolean = true

) : Persistable<Long> {

  override fun getId(): Long = id

  override fun isNew(): Boolean = new
}
