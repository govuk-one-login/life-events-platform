package uk.gov.gdx.datashare.repositories

import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@JaversSpringDataAuditable
interface AcquirerRepository : CrudRepository<Acquirer, UUID> {
  fun findAllByWhenDeletedIsNull(): List<Acquirer>

  @Query(
    "SELECT a.name FROM acquirer a " +
      "JOIN acquirer_subscription asub ON a.id = asub.acquirer_id " +
      "WHERE asub.id = :id ",
  )
  fun findNameForAcquirerSubscriptionId(id: UUID): String
}
