package uk.gov.gdx.datashare.repositories

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ConsumerSubscriptionEnrichmentFieldRepository : CrudRepository<ConsumerSubscriptionEnrichmentField, UUID> {
  @Query("SELECT * FROM consumer_subscription_enrichment_field csef WHERE csef.consumer_subscription_id = :consumerSubscriptionId")
  fun findAllByConsumerSubscriptionId(consumerSubscriptionId: UUID): List<ConsumerSubscriptionEnrichmentField>

  @Query("DELETE FROM consumer_subscription_enrichment_field csef WHERE csef.consumer_subscription_id = :consumerSubscriptionId")
  fun deleteAllByConsumerSubscriptionId(consumerSubscriptionId: UUID)
}
