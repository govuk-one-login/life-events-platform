package uk.gov.gdx.datashare.models

data class GroEnrichEventResponse(
  val statusCode: Number,
  val event: GroDeathRecord? = null,
)
