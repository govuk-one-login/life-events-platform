package uk.gov.gdx.datashare.uk.gov.gdx.datashare.helpers.builders

import uk.gov.gdx.datashare.repositories.Supplier
import java.time.LocalDateTime
import java.util.*

class SupplierBuilder(
  var id: UUID = UUID.randomUUID(),
  var name: String = "Test Supplier ${UUID.randomUUID()}",
  var whenCreated: LocalDateTime = LocalDateTime.now(),
  var whenDeleted: LocalDateTime? = null,
) {
  fun build(): Supplier {
    return Supplier(
      supplierId = id,
      name = name,
      whenCreated = whenCreated,
      whenDeleted = whenDeleted,
    )
  }
}
