package uk.gov.gdx.datashare.repositories

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import uk.gov.gdx.datashare.enums.DeathNotificationField
import java.util.*

data class AcquirerSubscriptionEnrichmentField(
  @Id
  @Column("id")
  val acquirerSubscriptionEnrichmentFieldId: UUID = UUID.randomUUID(),
  @Schema(description = "Acquirer subscription ID", required = true, example = "00000000-0000-0001-0000-000000000000")
  val acquirerSubscriptionId: UUID,
  @Schema(description = "Enrichment field name", required = true, example = "firstName")
  val enrichmentField: DeathNotificationField,

  @Transient
  @Value("false")
  val new: Boolean = true,

  ) : Persistable<UUID> {
  override fun getId() = acquirerSubscriptionEnrichmentFieldId

  override fun isNew(): Boolean = new
}
