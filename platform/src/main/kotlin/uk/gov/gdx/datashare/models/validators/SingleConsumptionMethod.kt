package uk.gov.gdx.datashare.models.validators

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [SingleConsumptionMethodAcquirerSubRequestValidator::class])
annotation class SingleConsumptionMethod(
  val message: String = "Exactly one of oauthClientId and queueUrl must be specified",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<Payload>> = [],
)
