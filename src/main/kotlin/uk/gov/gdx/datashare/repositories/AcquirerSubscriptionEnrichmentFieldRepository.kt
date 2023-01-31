package uk.gov.gdx.datashare.repositories

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AcquirerSubscriptionEnrichmentFieldRepository : CrudRepository<AcquirerSubscriptionEnrichmentField, UUID> {
  fun findAllByAcquirerSubscriptionId(acquirerSubscriptionId: UUID): List<AcquirerSubscriptionEnrichmentField>

  fun deleteAllByAcquirerSubscriptionId(acquirerSubscriptionId: UUID)
}
