package uk.gov.gdx.datashare.repository

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import java.time.LocalDateTime

data class ConsumerSubscription(
  @Id
  val id: Long,
  @Schema(description = "Consumer ID", required = true, example = "1")
  val consumerId: Long,
  @Schema(description = "Events Type", required = true, example = "DEATH_NOTIFICATION")
  val eventTypeId: String,
  @Schema(description = "Client ID used to poll event platform", required = false, example = "a-polling-client")
  val pollClientId: String?,
  @Schema(description = "Client ID used to callback to event platform", required = false, example = "a-callback-client")
  val callbackClientId: String?,
  val lastPollEventTime: LocalDateTime? = null,
  @Schema(description = "URI where to push data, can be s3 or http", required = false, example = "http://localhost/callback")
  val pushUri: String?,
  @Schema(description = "NI number required in response", required = false, example = "true", defaultValue = "false")
  val ninoRequired: Boolean = false,
  val whenCreated: LocalDateTime? = null,

  @Transient
  @Value("false")
  val new: Boolean = true

) : Persistable<Long> {

  override fun getId(): Long = id

  override fun isNew(): Boolean = new
}
