package uk.gov.gdx.datashare.repositories

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ConsumerSubscriptionEnrichmentFieldRepository : CrudRepository<ConsumerSubscriptionEnrichmentField, UUID> {
  fun findAllByConsumerSubscriptionId(consumerSubscriptionId: UUID): List<ConsumerSubscriptionEnrichmentField>

  fun deleteAllByConsumerSubscriptionId(consumerSubscriptionId: UUID)
}
