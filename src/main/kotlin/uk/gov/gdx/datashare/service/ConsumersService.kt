package uk.gov.gdx.datashare.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.gdx.datashare.config.ConsumerSubscriptionNotFoundException
import uk.gov.gdx.datashare.models.ConsumerRequest
import uk.gov.gdx.datashare.models.ConsumerSubRequest
import uk.gov.gdx.datashare.repository.*
import java.util.*

@Service
@Transactional
class ConsumersService(
  private val consumerSubscriptionRepository: ConsumerSubscriptionRepository,
  private val consumerRepository: ConsumerRepository,
) {
  fun getConsumers() = consumerRepository.findAll()

  fun getConsumerSubscriptions() = consumerSubscriptionRepository.findAll()

  fun getSubscriptionsForConsumer(consumerId: UUID) =
    consumerSubscriptionRepository.findAllByConsumerId(consumerId)

  fun addConsumerSubscription(
    consumerId: UUID,
    consumerSubRequest: ConsumerSubRequest,
  ): ConsumerSubscription {
    with(consumerSubRequest) {
      return consumerSubscriptionRepository.save(
        ConsumerSubscription(
          consumerId = consumerId,
          oauthClientId = oauthClientId,
          eventType = eventType,
          enrichmentFields = enrichmentFields,
        ),
      )
    }
  }

  fun updateConsumerSubscription(
    consumerId: UUID,
    subscriptionId: UUID,
    consumerSubRequest: ConsumerSubRequest,
  ): ConsumerSubscription {
    with(consumerSubRequest) {
      return consumerSubscriptionRepository.save(
        consumerSubscriptionRepository.findByIdOrNull(subscriptionId)?.copy(
          consumerId = consumerId,
          oauthClientId = oauthClientId,
          eventType = eventType,
          enrichmentFields = enrichmentFields,
          enrichmentFieldsIncludedInPoll = enrichmentFieldsIncludedInPoll,
        ) ?: throw ConsumerSubscriptionNotFoundException("Subscription $subscriptionId not found"),
      )
    }
  }

  fun addConsumer(
    consumerRequest: ConsumerRequest,
  ): Consumer {
    with(consumerRequest) {
      return consumerRepository.save(
        Consumer(
          name = name,
        ),
      )
    }
  }
}
