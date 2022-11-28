package uk.gov.gdx.datashare.service

import kotlinx.coroutines.flow.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.config.AuthenticationFacade
import uk.gov.gdx.datashare.repository.DataConsumerRepository
import uk.gov.gdx.datashare.repository.EventDataRepository
import uk.gov.gdx.datashare.resource.EventType
import uk.gov.gdx.datashare.resource.SubscribedEvent
import java.time.LocalDateTime

@Service
class EventPollService(
  private val eventDataRepository: EventDataRepository,
  private val authenticationFacade: AuthenticationFacade,
  private val dataConsumerRepository: DataConsumerRepository,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun getEvents(
    eventTypes: List<EventType>?,
    fromTime: LocalDateTime?,
    toTime: LocalDateTime?
  ): Flow<SubscribedEvent> {
    val oauthClient = authenticationFacade.getUsername()
    log.info("Looking up events for client {}", oauthClient)

    // check if client is allowed to poll
    val dataConsumer = dataConsumerRepository.findById(oauthClient)
      ?: throw RuntimeException("Client $oauthClient is not a known data consumer")

    val allowedTypes = dataConsumer.allowedEventTypes.split(",").map {
      EventType.valueOf(it)
    }

    // checks types requested are allowed
    val eventTypesToPoll = getTypeToPoll(eventTypes, allowedTypes, dataConsumer.clientName)

    // Retrieve events from event stream
    val now = LocalDateTime.now()
    val beginTime = fromTime ?: dataConsumer.lastPollEventTime ?: now.minusDays(1)
    val lastTime = toTime ?: now
    val events = eventDataRepository.findAllByEventTypes(
      eventTypesToPoll.map { it.toString() },
      beginTime,
      lastTime
    ).map {
      SubscribedEvent(
        eventType = EventType.valueOf(it.eventType),
        eventId = it.eventId,
        eventTime = it.whenCreated!!
      )
    }

    // save the time of the last record
    val lastPollEventTime = events.lastOrNull()?.eventTime ?: lastTime
    if (lastPollEventTime.isAfter(dataConsumer.lastPollEventTime)) {
      dataConsumerRepository.save(dataConsumer.copy(lastPollEventTime = lastPollEventTime))
    }

    log.info("Retrieved {} events between {} and {} for event types {}", events.count(), beginTime, lastTime, eventTypes)
    return events
  }

  private fun getTypeToPoll(
    eventTypes: List<EventType>?,
    allowedTypes: List<EventType>,
    clientName: String
  ) = eventTypes?.map {
    if (it !in allowedTypes) {
      throw RuntimeException("Client $clientName is not allowed to consume $it events")
    }
    it
  } ?: allowedTypes
}
