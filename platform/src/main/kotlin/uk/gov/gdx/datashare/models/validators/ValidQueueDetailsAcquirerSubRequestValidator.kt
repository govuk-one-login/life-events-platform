package uk.gov.gdx.datashare.models.validators

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import uk.gov.gdx.datashare.models.AcquirerSubRequest

class ValidQueueDetailsAcquirerSubRequestValidator :
  ConstraintValidator<ValidQueueDetails, AcquirerSubRequest> {
  override fun isValid(value: AcquirerSubRequest, context: ConstraintValidatorContext): Boolean {
    return (
      (value.queueName == null && value.principalArn == null) ||
        (value.queueName != null && value.principalArn != null)
      )
  }
}
