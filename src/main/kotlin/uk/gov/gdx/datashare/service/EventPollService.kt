package uk.gov.gdx.datashare.service

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.gdx.datashare.config.AuthenticationFacade
import uk.gov.gdx.datashare.repository.ConsumerSubscriptionRepository
import uk.gov.gdx.datashare.repository.EventDataRepository
import uk.gov.gdx.datashare.resource.SubscribedEvent
import java.time.LocalDateTime

@Service
class EventPollService(
  private val eventDataRepository: EventDataRepository,
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

    return consumerSubscriptionRepository.findAllByPollerClientId(authenticationFacade.getUsername())
      .filter { eventTypes.isNullOrEmpty() || it.eventTypeId in eventTypes }
      .flatMapMerge { sub ->
        val beginTime = fromTime ?: sub.lastPollEventTime ?: now.minusDays(1)
        val events = eventDataRepository.findAllByEventType(sub.eventTypeId, beginTime, lastPollEventTime)

        if (sub.lastPollEventTime == null || lastPollEventTime.isAfter(sub.lastPollEventTime)) {
          consumerSubscriptionRepository.updateLastPollTime(lastPollEventTime = lastPollEventTime, consumerId = sub.consumerId, sub.eventTypeId)
        }
        events
      }.map { event ->
        SubscribedEvent(
          eventType = event.eventTypeId,
          eventId = event.eventId,
          eventTime = event.whenCreated!!
        )
      }
  }
}
