package uk.gov.gdx.datashare.repositories

import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@JaversSpringDataAuditable
interface SupplierRepository : CrudRepository<Supplier, UUID> {
  @Override
  override fun findById(id: UUID) = findBySupplierIdAndWhenDeletedIsNull(id)
  fun findBySupplierIdAndWhenDeletedIsNull(id: UUID): Optional<Supplier>

  @Override
  override fun findAll() = findAllByWhenDeletedIsNull()
  fun findAllByWhenDeletedIsNull(): List<Supplier>
}
