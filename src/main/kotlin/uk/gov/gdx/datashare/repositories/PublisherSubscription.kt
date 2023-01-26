package uk.gov.gdx.datashare.repositories

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import uk.gov.gdx.datashare.enums.EventType
import java.time.LocalDateTime
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PublisherSubscription(
  @Id
  @Column("id")
  val publisherSubscriptionId: UUID = UUID.randomUUID(),
  @Schema(description = "Publisher ID", required = true, example = "00000000-0000-0001-0000-000000000000")
  val publisherId: UUID,
  @Schema(description = "Client ID", required = true, example = "a-client-id")
  val clientId: String,
  @Schema(description = "Events Type", required = true, example = "DEATH_NOTIFICATION")
  val eventType: EventType,
  val whenCreated: LocalDateTime = LocalDateTime.now(),

  @Transient
  @Value("false")
  @JsonIgnore
  val new: Boolean = true,

) : Persistable<UUID> {
  override fun getId(): UUID = publisherSubscriptionId

  override fun isNew(): Boolean = new
}
