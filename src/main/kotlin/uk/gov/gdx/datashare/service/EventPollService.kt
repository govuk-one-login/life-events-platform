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
import uk.gov.gdx.datashare.resource.SubscribedEvent
import java.time.LocalDateTime

@Service
class EventPollService(
  private val authenticationFacade: AuthenticationFacade,
  private val consumerSubscriptionRepository: ConsumerSubscriptionRepository,
  private val egressEventDataRepository: EgressEventDataRepository,
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
    val consumerSubscriptions = eventTypes?.let {
      consumerSubscriptionRepository.findAllByIngressEventTypesAndPollClientId(clientId, eventTypes)
        .toList()
        .associateBy({ it.consumerSubscriptionId }, {it.ingressEventType})
    }

    log.debug("Egress event types {} polled", consumerSubscriptions?.keys?.joinToString())

    return consumerSubscriptionRepository.findAllByPollClientId(authenticationFacade.getUsername())
      .filter { consumerSubscriptions.isNullOrEmpty() || it.consumerSubscriptionId in consumerSubscriptions.keys }
      .flatMapMerge { sub ->
        val beginTime = fromTime ?: sub.lastPollEventTime ?: now.minusDays(1)
        val events =
          egressEventDataRepository.findAllByConsumerSubscription(sub.consumerSubscriptionId, beginTime, lastPollEventTime)

        if (sub.lastPollEventTime == null || lastPollEventTime.isAfter(sub.lastPollEventTime)) {
          consumerSubscriptionRepository.updateLastPollTime(
            lastPollEventTime = lastPollEventTime,
            consumerId = sub.consumerId,
            sub.consumerSubscriptionId
          )
        }
        events
      }.map { event ->
        val eventType = consumerSubscriptions?.let {
          consumerSubscriptions[event.consumerSubscriptionId]
        } ?: consumerSubscriptionRepository.findById(event.consumerSubscriptionId)?.ingressEventType
          ?: throw RuntimeException("Consumer subscription ${event.consumerSubscriptionId} not found")

        SubscribedEvent(
          eventType = eventType,
          eventId = event.eventId,
          eventTime = event.whenCreated!!
        )
      }
  }
}
