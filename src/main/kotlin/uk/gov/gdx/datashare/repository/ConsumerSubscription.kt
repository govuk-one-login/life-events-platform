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
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ConsumerSubscription(
  @Id
  @Column("id")
  val consumerSubscriptionId: UUID = UUID.randomUUID(),
  @Schema(description = "Consumer ID", required = true, example = "00000000-0000-0001-0000-000000000000")
  val consumerId: UUID,
  @Schema(description = "Client ID used to access the event platform", required = false, example = "an-oauth-client")
  val oauthClientId: String? = null,
  @Schema(description = "Event type for this subscription", required = true, example = "DEATH_NOTIFICATION")
  val eventType: String,
  @Schema(
    description = "Indicates that the specified enrichment fields will be present when a poll of events occurs",
    required = false,
    defaultValue = "false",
    example = "false",
  )
  val enrichmentFieldsIncludedInPoll: Boolean = false,
  @Schema(
    description = "CSV List of required fields to which enrich the event",
    required = true,
    example = "firstNames,lastName,dateOfBirth",
  )
  val enrichmentFields: String,
  val whenCreated: LocalDateTime = LocalDateTime.now(),

  @Transient
  @Value("false")
  @JsonIgnore
  val new: Boolean = true,

) : Persistable<UUID> {

  override fun getId(): UUID = consumerSubscriptionId

  override fun isNew(): Boolean = new
}
