package uk.gov.gdx.datashare.helpers.builders

import uk.gov.gdx.datashare.repositories.Acquirer
import java.time.LocalDateTime
import java.util.*

class AcquirerBuilder(
  var id: UUID = UUID.randomUUID(),
  var name: String = "Test Acquirer ${UUID.randomUUID()}",
  var whenCreated: LocalDateTime = LocalDateTime.now(),
  var whenDeleted: LocalDateTime? = null,
) {
  fun build(): Acquirer {
    return Acquirer(
      acquirerId = id,
      name = name,
      whenCreated = whenCreated,
      whenDeleted = whenDeleted,
    )
  }
}
