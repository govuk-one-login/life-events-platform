package uk.gov.gdx.datashare.models.validators

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import uk.gov.gdx.datashare.models.CreateAcquirerRequest

class ValidQueueDetailsCreateAcquirerRequestValidator :
  ConstraintValidator<ValidQueueDetails, CreateAcquirerRequest> {
  override fun isValid(value: CreateAcquirerRequest, context: ConstraintValidatorContext): Boolean {
    return (
      (value.queueName == null && value.principalArn == null) ||
        (value.queueName != null && value.principalArn != null)
      )
  }
}
