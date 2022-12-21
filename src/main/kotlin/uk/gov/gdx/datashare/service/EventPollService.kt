package uk.gov.gdx.datashare.service

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.gdx.datashare.config.AuthenticationFacade
import uk.gov.gdx.datashare.repository.ConsumerSubscriptionRepository
import uk.gov.gdx.datashare.repository.EgressEventDataRepository
import uk.gov.gdx.datashare.repository.EgressEventTypeRepository
import uk.gov.gdx.datashare.resource.SubscribedEvent
import java.time.LocalDateTime

@Service
class EventPollService(
  private val egressEventDataRepository: EgressEventDataRepository,
  private val egressEventTypeRepository: EgressEventTypeRepository,
  private val authenticationFacade: AuthenticationFacade,
  private val consumerSubscriptionRepository: ConsumerSubscriptionRepository
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @OptIn(FlowPreview::class)
  @Transactional
  suspend fun getEvents(
    eventTypes: List<String>?,
    fromTime: LocalDateTime?,
    toTime: LocalDateTime?
  ): Flow<SubscribedEvent> {

    val now = LocalDateTime.now()
    val lastPollEventTime = toTime ?: now
    val clientId = authenticationFacade.getUsername()
    val egressEventTypes = eventTypes?.let {
      egressEventTypeRepository.findAllByIngressEventTypesAndClient(clientId, eventTypes)
        .toList()
        .associateBy({ it.eventTypeId }, {it.ingressEventType})
    }

    return consumerSubscriptionRepository.findAllByPollerClientId(authenticationFacade.getUsername())
      .filter { egressEventTypes.isNullOrEmpty() || it.eventTypeId in egressEventTypes.keys }
      .flatMapMerge { sub ->
        val beginTime = fromTime ?: sub.lastPollEventTime ?: now.minusDays(1)
        val events =
          egressEventDataRepository.findAllByEventType(sub.eventTypeId, beginTime, lastPollEventTime)

        if (sub.lastPollEventTime == null || lastPollEventTime.isAfter(sub.lastPollEventTime)) {
          consumerSubscriptionRepository.updateLastPollTime(
            lastPollEventTime = lastPollEventTime,
            consumerId = sub.consumerId,
            sub.eventTypeId
          )
        }
        events
      }.map { event ->
        val eventType = egressEventTypes?.let {
          egressEventTypes[event.typeId]
        } ?: egressEventTypeRepository.findById(event.typeId)?.ingressEventType
          ?: throw RuntimeException("Event type $event.typeId Not Found")

        SubscribedEvent(
          eventType = eventType,
          eventId = event.eventId,
          eventTime = event.whenCreated!!
        )
      }
  }
}
