package uk.gov.gdx.datashare.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.repository.ConsumerRepository
import uk.gov.gdx.datashare.repository.ConsumerSubscription
import uk.gov.gdx.datashare.repository.ConsumerSubscriptionRepository
import uk.gov.gdx.datashare.repository.EgressEventData
import uk.gov.gdx.datashare.repository.IngressEventTypeRepository
import java.util.UUID

@Service
class EventPublishingService(
  private val dataShareTopicService: DataShareTopicService,
  private val consumerSubscriptionRepository: ConsumerSubscriptionRepository,
  private val consumerRepository: ConsumerRepository,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun storeAndPublishEvent(egressEventData: EgressEventData) {

    consumerSubscriptionRepository.findById(egressEventData.consumerSubscriptionId)?.let { sub ->
      consumerRepository.findById(sub.consumerId)?.let {
        log.debug(
          "Publishing new event {} {} for {}",
          sub.ingressEventType,
          egressEventData.eventId,
          it.name
        )

        dataShareTopicService.sendGovEvent(
          eventId = egressEventData.eventId,
          consumer = it.name,
          eventType = sub.ingressEventType,
          occurredAt = egressEventData.whenCreated!!
        )
      }
    }
  }
}
