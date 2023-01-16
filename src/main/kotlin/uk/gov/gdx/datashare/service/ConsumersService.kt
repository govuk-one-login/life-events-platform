package uk.gov.gdx.datashare.service

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.gdx.datashare.config.ConsumerSubscriptionNotFoundException
import uk.gov.gdx.datashare.repository.*
import java.util.*

@Service
@Transactional
class ConsumersService(
  private val consumerSubscriptionRepository: ConsumerSubscriptionRepository,
  private val consumerRepository: ConsumerRepository,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun getConsumers() = consumerRepository.findAll()

  suspend fun getConsumerSubscriptions() = consumerSubscriptionRepository.findAll()

  suspend fun getSubscriptionsForConsumer(consumerId: UUID) =
    consumerSubscriptionRepository.findAllByConsumerId(consumerId)

  suspend fun addConsumerSubscription(
    consumerId: UUID,
    consumerSubRequest: ConsumerSubRequest,
  ): ConsumerSubscription {
    with(consumerSubRequest) {
      return consumerSubscriptionRepository.save(
        ConsumerSubscription(
          consumerId = consumerId,
          oauthClientId = oauthClientId,
          pushUri = pushUri,
          ingressEventType = ingressEventType,
          enrichmentFields = enrichmentFields,
        ),
      )
    }
  }

  suspend fun updateConsumerSubscription(
    consumerId: UUID,
    subscriptionId: UUID,
    consumerSubRequest: ConsumerSubRequest,
  ): ConsumerSubscription {
    with(consumerSubRequest) {
      return consumerSubscriptionRepository.save(
        consumerSubscriptionRepository.findById(subscriptionId)?.copy(
          consumerId = consumerId,
          oauthClientId = oauthClientId,
          pushUri = pushUri,
          ingressEventType = ingressEventType,
          enrichmentFields = enrichmentFields,
        ) ?: throw ConsumerSubscriptionNotFoundException("Subscription $subscriptionId not found"),
      )
    }
  }

  suspend fun addConsumer(
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

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Consumer Subscription Request")
data class ConsumerSubRequest(
  @Schema(description = "Events Type", required = true, example = "DEATH_NOTIFICATION")
  val ingressEventType: String,
  @Schema(description = "Client ID used to access event platform", required = false, example = "an-oauth-client")
  val oauthClientId: String? = null,
  @Schema(
    description = "URI where to push data, can be s3 or http",
    required = false,
    example = "http://localhost/callback",
  )
  val pushUri: String? = null,
  @Schema(
    description = "CSV List of required fields to enrich the event with",
    required = true,
    example = "firstName,lastName",
  )
  val enrichmentFields: String,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Consumer Request")
data class ConsumerRequest(
  @Schema(description = "Consumer name", required = true, example = "DWP")
  val name: String,
)
