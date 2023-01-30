package uk.gov.gdx.datashare.services

import com.amazonaws.xray.spring.aop.XRayEnabled
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.gdx.datashare.config.ConsumerSubscriptionNotFoundException
import uk.gov.gdx.datashare.models.ConsumerRequest
import uk.gov.gdx.datashare.models.ConsumerSubRequest
import uk.gov.gdx.datashare.models.ConsumerSubscriptionDto
import uk.gov.gdx.datashare.repositories.*
import java.util.*

@Service
@Transactional
@XRayEnabled
class ConsumersService(
  private val consumerSubscriptionRepository: ConsumerSubscriptionRepository,
  private val consumerRepository: ConsumerRepository,
  private val consumerSubscriptionEnrichmentFieldRepository: ConsumerSubscriptionEnrichmentFieldRepository,
) {
  fun getConsumers() = consumerRepository.findAll()

  fun getConsumerSubscriptions() = consumerSubscriptionRepository.findAll()

  fun getSubscriptionsForConsumer(consumerId: UUID) =
    consumerSubscriptionRepository.findAllByConsumerId(consumerId)

  fun addConsumerSubscription(
    consumerId: UUID,
    consumerSubRequest: ConsumerSubRequest,
  ): ConsumerSubscriptionDto {
    with(consumerSubRequest) {
      val consumerSubscription = consumerSubscriptionRepository.save(
        ConsumerSubscription(
          consumerId = consumerId,
          oauthClientId = oauthClientId,
          eventType = eventType,
        ),
      )
      val enrichmentFields =
        addConsumerSubscriptionEnrichmentFields(consumerSubscription.consumerSubscriptionId, enrichmentFields)

      return mapConsumerSubscriptionDto(consumerSubscription, enrichmentFields)
    }
  }

  fun updateConsumerSubscription(
    consumerId: UUID,
    subscriptionId: UUID,
    consumerSubRequest: ConsumerSubRequest,
  ): ConsumerSubscriptionDto {
    with(consumerSubRequest) {
      val consumerSubscription = consumerSubscriptionRepository.save(
        consumerSubscriptionRepository.findByIdOrNull(subscriptionId)?.copy(
          consumerId = consumerId,
          oauthClientId = oauthClientId,
          eventType = eventType,
          enrichmentFieldsIncludedInPoll = enrichmentFieldsIncludedInPoll,
        ) ?: throw ConsumerSubscriptionNotFoundException("Subscription $subscriptionId not found"),
      )

      consumerSubscriptionEnrichmentFieldRepository.deleteAllByConsumerSubscriptionId(consumerSubscription.consumerSubscriptionId)
      val enrichmentFields =
        addConsumerSubscriptionEnrichmentFields(consumerSubscription.consumerSubscriptionId, enrichmentFields)

      return mapConsumerSubscriptionDto(consumerSubscription, enrichmentFields)
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
