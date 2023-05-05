package uk.gov.gdx.datashare.models

import uk.gov.gdx.datashare.enums.GroSex
import java.time.LocalDate

data class GroDeathRecord(
  val hash: String,
  val registrationId: String,
  val eventTime: LocalDate? = null,
  val verificationLevel: String,
  val dateOfDeath: LocalDate? = null,
  val partialMonthOfDeath: String,
  val partialYearOfDeath: String,
  val forenames: String,
  val surname: String,
  val maidenSurname: String,
  val sex: GroSex? = null,
  val dateOfBirth: LocalDate? = null,
  val addressLine1: String,
  val addressLine2: String,
  val addressLine3: String,
  val addressLine4: String,
  val postcode: String,
)
