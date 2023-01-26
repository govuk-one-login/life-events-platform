package uk.gov.gdx.datashare.models

import java.time.LocalDate

data class LevDeathRecord(
  val id: String,
  val date: LocalDate? = null,
  val deceased: LevDeceased,
  val status: LevDeathRecordStatus? = null,
)
