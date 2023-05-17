package uk.gov.gdx.datashare.models

interface GroEventResponse<T> {
  val statusCode: Number
  val payload: T
}

data class GroEnrichEventResponse(
  override val statusCode: Number,
  override val payload: GroDeathRecord? = null,
) : GroEventResponse<GroDeathRecord?>

data class GroDeleteEventResponse(
  override val statusCode: Number,
  override val payload: String,
) : GroEventResponse<String>
