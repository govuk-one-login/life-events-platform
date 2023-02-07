package uk.gov.gdx.datashare.repositories

import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@JaversSpringDataAuditable
interface AcquirerSubscriptionEnrichmentFieldRepository : CrudRepository<AcquirerSubscriptionEnrichmentField, UUID> {
  fun findAllByAcquirerSubscriptionId(acquirerSubscriptionId: UUID): List<AcquirerSubscriptionEnrichmentField>

  fun deleteAllByAcquirerSubscriptionId(acquirerSubscriptionId: UUID)
}
