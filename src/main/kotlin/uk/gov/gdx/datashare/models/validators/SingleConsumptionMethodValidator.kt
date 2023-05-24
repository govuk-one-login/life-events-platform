package uk.gov.gdx.datashare.models.validators

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import uk.gov.gdx.datashare.models.AcquirerSubRequest

class SingleConsumptionMethodValidator : ConstraintValidator<SingleConsumptionMethod, AcquirerSubRequest> {
  override fun isValid(value: AcquirerSubRequest, context: ConstraintValidatorContext): Boolean {
    if (value.oauthClientId == null && value.queueName == null) {
      return false
    }

    if (value.oauthClientId != null && value.queueName != null) {
      return false
    }

    if ((value.queueName == null && value.principalArn != null) ||
      (value.queueName != null && value.principalArn == null)
    ) {
      context.disableDefaultConstraintViolation()
      context.buildConstraintViolationWithTemplate("Both queueName and principalArn must be specified or both must be null")
        .addConstraintViolation()

      return false
    }

    return true
  }
}
