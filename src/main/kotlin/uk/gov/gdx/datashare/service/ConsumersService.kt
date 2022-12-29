package uk.gov.gdx.datashare.service

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.gdx.datashare.repository.*
import java.util.*

@Service
@Transactional
class ConsumersService(
  private val consumerSubscriptionRepository: ConsumerSubscriptionRepository,
  private val consumerRepository: ConsumerRepository,
  private val egressEventTypeRepository: EgressEventTypeRepository
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun getConsumers() = consumerRepository.findAll()

  suspend fun getConsumerSubscriptions() = consumerSubscriptionRepository.findAll()

  suspend fun getSubscriptionsForConsumer(consumerId: UUID) = consumerSubscriptionRepository.findAllByConsumerId(consumerId)

  suspend fun addConsumerSubscription(
    consumerId: UUID,
    consumerSubRequest: ConsumerSubRequest
  ): ConsumerSubscription {
    with(consumerSubRequest) {
      val consumer = consumerRepository.findById(consumerId) ?: throw RuntimeException("Consumer $consumerId not found")

      val egressEventType = EgressEventType(
        ingressEventType = ingressEventType,
        description = "$ingressEventType for ${consumer.name}",
        enrichmentFields = enrichmentFields
      )

      egressEventTypeRepository.save(egressEventType)

      return consumerSubscriptionRepository.save(
        ConsumerSubscription(
          consumerId = consumerId,
          pollClientId = pollClientId,
          eventTypeId = egressEventType.eventTypeId,
          callbackClientId = callbackClientId,
          pushUri = pushUri,
          ninoRequired = ninoRequired,
          consumerSubscriptionId = UUID.randomUUID()
        )
      )
    }
  }

  suspend fun updateConsumerSubscription(
    consumerId: UUID,
    subscriptionId: UUID,
    consumerSubRequest: ConsumerSubRequest
  ): ConsumerSubscription {
    with(consumerSubRequest) {
      val consumer = consumerRepository.findById(consumerId) ?: throw RuntimeException("Consumer $consumerId not found")

      val existingEgressEventType = egressEventTypeRepository.findByIngressEventTypeAndConsumerId(ingressEventType, consumerId)
      val egressEventType = existingEgressEventType ?: EgressEventType(
        ingressEventType = ingressEventType,
        description = "$ingressEventType for ${consumer.name}",
        enrichmentFields = enrichmentFields
      )
      if (existingEgressEventType == null) {
        egressEventTypeRepository.save(egressEventType)
      }

      return consumerSubscriptionRepository.save(
        consumerSubscriptionRepository.findById(subscriptionId)?.copy(
          consumerId = consumerId,
          pollClientId = pollClientId,
          eventTypeId = egressEventType.eventTypeId,
          callbackClientId = callbackClientId,
          pushUri = pushUri,
          ninoRequired = ninoRequired
        ) ?: throw RuntimeException("Subscription not found")
      )
    }
  }

  suspend fun addConsumer(
    consumerRequest: ConsumerRequest
  ): Consumer {
    with(consumerRequest) {
      return consumerRepository.save(
        Consumer(
          name = name
        )
      )
    }
  }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Consumer Subscription Request")
data class ConsumerSubRequest(
  @Schema(description = "Events Type", required = true, example = "DEATH_NOTIFICATION")
  val ingressEventType: String,
  @Schema(description = "Client ID used to poll event platform", required = false, example = "a-polling-client")
  val pollClientId: String? = null,
  @Schema(description = "Client ID used to callback to event platform", required = false, example = "a-callback-client")
  val callbackClientId: String? = null,
  @Schema(description = "URI where to push data, can be s3 or http", required = false, example = "http://localhost/callback")
  val pushUri: String? = null,
  @Schema(description = "CSV List of required fields to enrich the event with", required = true, example = "firstName,lastName")
  val enrichmentFields: String,
  @Schema(description = "NI number required in response", required = false, example = "true", defaultValue = "false")
  val ninoRequired: Boolean = false,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Consumer Request")
data class ConsumerRequest(
  @Schema(description = "Consumer name", required = true, example = "DWP")
  val name: String,
)