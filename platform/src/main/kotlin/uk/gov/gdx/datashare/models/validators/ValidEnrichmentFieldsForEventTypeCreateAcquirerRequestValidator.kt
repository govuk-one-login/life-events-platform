package uk.gov.gdx.datashare.models.validators

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventTypeEnrichmentFieldsRelationship
import uk.gov.gdx.datashare.models.CreateAcquirerRequest

class ValidEnrichmentFieldsForEventTypeCreateAcquirerRequestValidator :
  ConstraintValidator<ValidEnrichmentFieldsForEventType, CreateAcquirerRequest> {
  override fun isValid(value: CreateAcquirerRequest, context: ConstraintValidatorContext): Boolean {
    val eventTypeEnrichmentFields = EventTypeEnrichmentFieldsRelationship[value.eventType]
    val invalidEnrichmentFields = mutableListOf<EnrichmentField>()
    value.enrichmentFields.forEach {
      if (!eventTypeEnrichmentFields!!.contains(it)) {
        invalidEnrichmentFields.add(it)
      }
    }
    return invalidEnrichmentFields.isEmpty()
  }
}
