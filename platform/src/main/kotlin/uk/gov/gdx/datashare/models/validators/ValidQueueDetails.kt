package uk.gov.gdx.datashare.models.validators

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(
  validatedBy = [
    ValidQueueDetailsAcquirerSubRequestValidator::class,
    ValidQueueDetailsCreateAcquirerRequestValidator::class,
  ],
)
annotation class ValidQueueDetails(
  val message: String = "Both queueName and principalArn must be specified or both must be null",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<Payload>> = [],
)
