package uk.gov.gdx.datashare.repositories

import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@JaversSpringDataAuditable
interface AcquirerSubscriptionEnrichmentFieldRepository : CrudRepository<AcquirerSubscriptionEnrichmentField, UUID> {
  fun findAllByAcquirerSubscriptionId(acquirerSubscriptionId: UUID): List<AcquirerSubscriptionEnrichmentField>

  @Query("DELETE FROM acquirer_subscription_enrichment_field WHERE acquirer_subscription_id = :acquirerSubscriptionId")
  @Modifying
  fun deleteAllByAcquirerSubscriptionId(acquirerSubscriptionId: UUID)
}
