package uk.gov.gdx.datashare.repository

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ConsumerSubscription(
  @Id
  @Column("id")
  val consumerSubscriptionId: UUID = UUID.randomUUID(),
  @Schema(description = "Consumer ID", required = true, example = "00000000-0000-0001-0000-000000000000")
  val consumerId: UUID,
  @Schema(description = "Client ID used to poll event platform", required = false, example = "a-polling-client")
  val pollClientId: String? = null,
  @Schema(description = "Client ID used to callback to event platform", required = false, example = "a-callback-client")
  val callbackClientId: String? = null,
  val lastPollEventTime: LocalDateTime? = null,
  @JsonIgnore
  @Schema(
    description = "URI where to push data, can be s3 or http",
    required = false,
    example = "http://localhost/callback"
  )
  val pushUri: String? = null,
  @Schema(description = "NI number required in response", required = false, example = "true", defaultValue = "false")
  val ninoRequired: Boolean = false,
  @Schema(
    description = "Is consumer subscription a legacy subscription",
    required = false,
    example = "true",
    defaultValue = "false"
  )
  val isLegacy: Boolean = false,
  @Schema(description = "Events Type of Ingress Notification", required = true, example = "DEATH_NOTIFICATION")
  val ingressEventType: String,
  @Schema(
    description = "CSV List of required fields to enrich the event with",
    required = true,
    example = "firstName,lastName"
  )
  val enrichmentFields: String,
  val whenCreated: LocalDateTime? = null,

  @Transient
  @Value("false")
  @JsonIgnore
  val new: Boolean = true

) : Persistable<UUID> {

  override fun getId(): UUID = consumerSubscriptionId

  override fun isNew(): Boolean = new
}
