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

  fun getConsumerSubscriptions(): List<ConsumerSubscriptionDto> {
    val consumerSubscriptions = consumerSubscriptionRepository.findAll()
    return consumerSubscriptions.map {
      val enrichmentFields =
        consumerSubscriptionEnrichmentFieldRepository.findAllByConsumerSubscriptionId(it.consumerSubscriptionId)
      mapConsumerSubscriptionDto(it, enrichmentFields)
    }
  }

  fun getSubscriptionsForConsumer(consumerId: UUID): List<ConsumerSubscriptionDto> {
    val consumerSubscriptions = consumerSubscriptionRepository.findAllByConsumerId(consumerId)
    return consumerSubscriptions.map {
      val enrichmentFields =
        consumerSubscriptionEnrichmentFieldRepository.findAllByConsumerSubscriptionId(it.consumerSubscriptionId)
      mapConsumerSubscriptionDto(it, enrichmentFields)
    }
  }

  private fun mapConsumerSubscriptionDto(
    consumerSubscription: ConsumerSubscription,
    enrichmentFields: List<ConsumerSubscriptionEnrichmentField>,
  ): ConsumerSubscriptionDto {
    return ConsumerSubscriptionDto(
      consumerSubscriptionId = consumerSubscription.consumerSubscriptionId,
      consumerId = consumerSubscription.consumerId,
      oauthClientId = consumerSubscription.oauthClientId,
      eventType = consumerSubscription.eventType,
      enrichmentFields = enrichmentFields.map { it.enrichmentField },
      enrichmentFieldsIncludedInPoll = consumerSubscription.enrichmentFieldsIncludedInPoll,
      whenCreated = consumerSubscription.whenCreated,
    )
  }

  private fun addConsumerSubscriptionEnrichmentFields(
    consumerSubscriptionId: UUID,
    enrichmentFields: List<String>,
  ): List<ConsumerSubscriptionEnrichmentField> {
    return consumerSubscriptionEnrichmentFieldRepository.saveAll(
      enrichmentFields.map {
        ConsumerSubscriptionEnrichmentField(
          consumerSubscriptionId = consumerSubscriptionId,
          enrichmentField = it,
        )
      },
    ).toList()
  }

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
