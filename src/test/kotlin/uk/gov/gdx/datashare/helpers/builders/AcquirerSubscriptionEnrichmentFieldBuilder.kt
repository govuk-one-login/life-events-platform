package uk.gov.gdx.datashare.uk.gov.gdx.datashare.helpers.builders

import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.repositories.AcquirerSubscriptionEnrichmentField
import java.util.*

data class AcquirerSubscriptionEnrichmentFieldBuilder(
  var acquirerSubscriptionEnrichmentFieldId: UUID = UUID.randomUUID(),
  var acquirerSubscriptionId: UUID = UUID.randomUUID(),
  var enrichmentField: EnrichmentField = EnrichmentField.FIRST_NAMES,
) {
  fun build(): AcquirerSubscriptionEnrichmentField {
    return AcquirerSubscriptionEnrichmentField(
      acquirerSubscriptionEnrichmentFieldId = acquirerSubscriptionEnrichmentFieldId,
      acquirerSubscriptionId = acquirerSubscriptionId,
      enrichmentField = enrichmentField,
    )
  }
}
