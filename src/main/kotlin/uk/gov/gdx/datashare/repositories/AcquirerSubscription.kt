package uk.gov.gdx.datashare.repositories

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import uk.gov.gdx.datashare.enums.EventType
import java.time.LocalDateTime
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AcquirerSubscription(
  @Id
  @Column("id")
  val acquirerSubscriptionId: UUID = UUID.randomUUID(),
  val acquirerId: UUID,
  val oauthClientId: String? = null,
  val eventType: EventType,
  val enrichmentFieldsIncludedInPoll: Boolean = false,
  val whenCreated: LocalDateTime = LocalDateTime.now(),

  @Transient
  @Value("false")
  val new: Boolean = true,

) : Persistable<UUID> {

  override fun getId(): UUID = acquirerSubscriptionId

  override fun isNew(): Boolean = new
}
