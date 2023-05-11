package uk.gov.gdx.datashare.models

data class GroEnrichEventResponse(
  val StatusCode: Number,
  val Event: GroDeathRecord? = null,
)
