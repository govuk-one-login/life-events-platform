package uk.gov.gdx.datashare.models.validators

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(
  validatedBy = [
    ValidEnrichmentFieldsForEventTypeAcquirerSubRequestValidator::class,
    ValidEnrichmentFieldsForEventTypeCreateAcquirerRequestValidator::class,
  ],
)
annotation class ValidEnrichmentFieldsForEventType(
  val message: String = "Enrichment fields much be valid for the given EventType",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<Payload>> = [],
)
