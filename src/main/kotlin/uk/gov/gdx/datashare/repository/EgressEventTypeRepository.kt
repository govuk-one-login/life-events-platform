package uk.gov.gdx.datashare.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface EgressEventTypeRepository : CoroutineCrudRepository<EgressEventType, UUID> {
  @Query("SELECT et.* FROM egress_event_type et " +
    "JOIN consumer_subscription cs ON cs.event_type_id = et.id " +
    "AND cs.poll_client_id = :clientId " +
    "WHERE et.ingress_event_type IN (:ingressEventTypes) ")
  fun findAllByIngressEventTypesAndClient(clientId: String, ingressEventTypes: List<String>): Flow<EgressEventType>

  @Query("SELECT et.* FROM egress_event_type et " +
    "JOIN consumer_subscription cs ON cs.event_type_id = et.id " +
    "AND cs.id = :consumerId " +
    "WHERE et.ingress_event_type = :ingressEventType ")
  fun findByIngressEventTypeAndConsumerId(ingressEventType: String, consumerId: UUID): EgressEventType?

  @Query("SELECT et.* FROM egress_event_type et " +
    "WHERE et.ingress_event_type = :ingressEventType ")
  fun findAllByIngressEventType(ingressEventType: String): Flow<EgressEventType>
}
