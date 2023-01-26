package uk.gov.gdx.datashare.models

import uk.gov.gdx.datashare.enums.Sex
import java.time.LocalDate

data class LevDeceased(
  val forenames: String? = null,
  val surname: String? = null,
  val dateOfDeath: LocalDate? = null,
  val sex: Sex? = null,
  val maidenSurname: String? = null,
  val birthplace: String? = null,
  val dateOfBirth: LocalDate? = null,
  val deathplace: String? = null,
  val occupation: String? = null,
  val retired: Boolean? = null,
  val address: String? = null,
)
