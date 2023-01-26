package uk.gov.gdx.datashare.service

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.gdx.datashare.config.ConsumerSubscriptionNotFoundException
import uk.gov.gdx.datashare.enums.EventType
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

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Consumer Subscription Request")
data class ConsumerSubRequest(
  @Schema(description = "Events Type", required = true, example = "DEATH_NOTIFICATION")
  val eventType: EventType,
  @Schema(description = "Client ID used to access event platform", required = false, example = "an-oauth-client")
  val oauthClientId: String? = null,
  @Schema(
    description = "CSV List of required fields to enrich the event with",
    required = true,
    example = "firstNames,lastName",
  )
  val enrichmentFields: String,
  @Schema(
    description = "Indicates that the specified enrichment fields will be present when a poll of events occurs",
    required = false,
    defaultValue = "false",
    example = "false",
  )
  val enrichmentFieldsIncludedInPoll: Boolean = false,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Consumer Request")
data class ConsumerRequest(
  @Schema(description = "Consumer name", required = true, example = "DWP")
  val name: String,
)
