package uk.gov.gdx.datashare.service

import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.webjars.NotFoundException
import uk.gov.gdx.datashare.config.AuthenticationFacade
import uk.gov.gdx.datashare.repository.EgressEventDataRepository
import uk.gov.gdx.datashare.repository.IngressEventDataRepository
import java.util.*

@Service
class EventDataService(
  private val authenticationFacade: AuthenticationFacade,
  private val egressEventDataRepository: EgressEventDataRepository,
  private val ingressEventDataRepository: IngressEventDataRepository,
) {
  @Transactional
  suspend fun deleteEvent(id: UUID) {
    val pollingClientId = authenticationFacade.getUsername()
    val egressEvent = egressEventDataRepository.findByPollerClientIdAndId(pollingClientId, id)
      ?: throw NotFoundException("Egress event $id not found for polling client $pollingClientId")

    egressEventDataRepository.deleteById(egressEvent.id)

    val remainingEvents = egressEventDataRepository.findAllByIngressEventId(egressEvent.ingressEventId).toList()
    if (remainingEvents.isEmpty()) {
      ingressEventDataRepository.deleteById(egressEvent.ingressEventId)
    }
  }
}