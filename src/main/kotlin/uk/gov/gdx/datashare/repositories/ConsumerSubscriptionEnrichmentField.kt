package uk.gov.gdx.datashare.repositories

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import java.util.*

data class ConsumerSubscriptionEnrichmentField(
  @Id
  @Column("id")
  val consumerSubscriptionEnrichmentFieldId: UUID = UUID.randomUUID(),
  @Schema(description = "Consumer subscription ID", required = true, example = "00000000-0000-0001-0000-000000000000")
  val consumerSubscriptionId: UUID,
  @Schema(description = "Enrichment field name", required = true, example = "firstName")
  val enrichmentField: String,

  @Transient
  @Value("false")
  val new: Boolean = true,

) : Persistable<UUID> {
  override fun getId() = consumerSubscriptionEnrichmentFieldId

  override fun isNew(): Boolean = new
}
