package uk.gov.gdx.datashare.models

import uk.gov.gdx.datashare.enums.EnrichmentField

class TestEventDetails(
  val testField: String? = null,
) : EventDetails {
  override fun maskedCopy(enrichmentFields: List<EnrichmentField>): EventDetails {
    return TestEventDetails(
      testField = testField
    )
  }
}
