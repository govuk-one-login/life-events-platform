package uk.gov.gdx.datashare.models

import java.time.LocalDate

data class PrisonerRecord(
  val prisonerNumber: String,
  val firstName: String,
  val middleNames: String?,
  val lastName: String,
  val dateOfBirth: LocalDate,
  val gender: String,
)
