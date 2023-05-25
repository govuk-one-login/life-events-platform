package uk.gov.gdx.datashare.models.validators

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import uk.gov.gdx.datashare.models.AcquirerSubRequest

class SingleConsumptionMethodAcquirerSubRequestValidator : ConstraintValidator<SingleConsumptionMethod, AcquirerSubRequest> {
  override fun isValid(value: AcquirerSubRequest, context: ConstraintValidatorContext): Boolean {
    return (
      (value.oauthClientId != null && value.queueName == null) ||
        (value.oauthClientId == null && value.queueName != null)
      )
  }
}
