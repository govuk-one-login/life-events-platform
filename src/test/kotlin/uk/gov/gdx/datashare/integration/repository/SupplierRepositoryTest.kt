package uk.gov.gdx.datashare.integration.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.gdx.datashare.repositories.SupplierRepository
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.helpers.builders.SupplierBuilder
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.integration.MockIntegrationTestBase
import java.time.LocalDateTime

class SupplierRepositoryTest(@Autowired private val supplierRepository: SupplierRepository) :
  MockIntegrationTestBase() {
  @Test
  fun `findAllByWhenDeletedIsNull returns the correct values`() {
    val returnedSupplier = supplierRepository.save(SupplierBuilder().build())
    val notReturnedSupplier = supplierRepository.save(SupplierBuilder(whenDeleted = LocalDateTime.now()).build())

    val suppliers = supplierRepository.findAllByWhenDeletedIsNull()

    assertThat(
      suppliers.filter { s -> s.supplierId == returnedSupplier.id },
    )
      .hasSize(1)

    assertThat(
      suppliers.filter { s -> s.supplierId == notReturnedSupplier.id },
    )
      .isEmpty()
  }
}
