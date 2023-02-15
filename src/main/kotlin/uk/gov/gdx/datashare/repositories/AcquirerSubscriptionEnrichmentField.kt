package uk.gov.gdx.datashare.repositories

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import uk.gov.gdx.datashare.enums.EnrichmentField
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Acquirer Subscription Enrichment Field")
data class AcquirerSubscriptionEnrichmentField(
  @Id
  @Column("id")
  val acquirerSubscriptionEnrichmentFieldId: UUID = UUID.randomUUID(),
  @Schema(description = "Acquirer subscription ID", required = true, example = "00000000-0000-0001-0000-000000000000", pattern = UUID_REGEX)
  val acquirerSubscriptionId: UUID,
  @Schema(description = "Enrichment field name", required = true, example = "firstName")
  val enrichmentField: EnrichmentField,

  @Transient
  @Value("false")
  val new: Boolean = true,

) : Persistable<UUID> {
  @JsonIgnore
  override fun getId() = acquirerSubscriptionEnrichmentFieldId

  override fun isNew(): Boolean = new
}
