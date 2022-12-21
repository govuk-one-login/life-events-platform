package uk.gov.gdx.datashare.service

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.gdx.datashare.repository.ConsumerSubscription
import uk.gov.gdx.datashare.repository.ConsumerSubscriptionRepository
import uk.gov.gdx.datashare.repository.EventConsumerRepository
import uk.gov.gdx.datashare.repository.EventPublisherRepository
import uk.gov.gdx.datashare.repository.EventSubscription
import uk.gov.gdx.datashare.repository.EventSubscriptionRepository
import java.util.*

@Service
@Transactional
class SubscriptionManagerService(
  private val eventSubscriptionRepository: EventSubscriptionRepository,
  private val consumerSubscriptionRepository: ConsumerSubscriptionRepository,
  private val eventPublisherRepository: EventPublisherRepository,
  private val eventConsumerRepository: EventConsumerRepository
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getPublishers() = eventPublisherRepository.findAll()

  fun getEventSubscriptions() = eventSubscriptionRepository.findAll()

  fun getConsumers() = eventConsumerRepository.findAll()

  fun getConsumersSubscriptions() = consumerSubscriptionRepository.findAll()

  suspend fun addEventSubscription(
    publisherSubRequest: PublisherSubRequest
  ): EventSubscription {
    with(publisherSubRequest) {
      return eventSubscriptionRepository.save(
        EventSubscription(
          publisherId = publisherId,
          clientId = clientId,
          eventTypeId = eventTypeId,
          datasetId = datasetId,
          eventSubscriptionId = UUID.randomUUID()
        )
      )
    }
  }

  suspend fun updateEventSubscription(
    subscriptionId: UUID,
    publisherSubRequest: PublisherSubRequest
  ): EventSubscription {
    with(publisherSubRequest) {
      return eventSubscriptionRepository.save(
        eventSubscriptionRepository.findById(subscriptionId)?.copy(
          publisherId = publisherId,
          clientId = clientId,
          eventTypeId = eventTypeId,
          datasetId = datasetId
        ) ?: throw RuntimeException("Subscription not found")
      )
    }
  }

  suspend fun addConsumerSubscription(
    consumerSubRequest: ConsumerSubRequest
  ): ConsumerSubscription {
    with(consumerSubRequest) {
      return consumerSubscriptionRepository.save(
        ConsumerSubscription(
          consumerId = consumerId,
          pollClientId = pollClientId,
          eventTypeId = eventTypeId,
          callbackClientId = callbackClientId,
          pushUri = pushUri,
          ninoRequired = ninoRequired,
          consumerSubscriptionId = UUID.randomUUID()
        )
      )
    }
  }

  suspend fun updateConsumerSubscription(
    subId: UUID,
    consumerSubRequest: ConsumerSubRequest
  ): ConsumerSubscription {
    with(consumerSubRequest) {
      return consumerSubscriptionRepository.save(
        consumerSubscriptionRepository.findById(subId)?.copy(
          consumerId = consumerId,
          pollClientId = pollClientId,
          eventTypeId = eventTypeId,
          callbackClientId = callbackClientId,
          pushUri = pushUri,
          ninoRequired = ninoRequired
        ) ?: throw RuntimeException("Subscription not found")
      )
    }
  }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Publisher Subscription Request")
data class PublisherSubRequest(
  @Schema(description = "Publisher ID", required = true, example = "00000000-0000-0001-0000-000000000000")
  val publisherId: UUID,
  @Schema(description = "Client ID", required = true, example = "a-client-id")
  val clientId: String,
  @Schema(description = "Events Type", required = true, example = "DEATH_NOTIFICATION")
  val eventTypeId: String,
  @Schema(description = "Data Set", required = true, example = "DEATH_LEN")
  val datasetId: String
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Consumer Subscription Request")
data class ConsumerSubRequest(
  @Schema(description = "Consumer ID", required = true, example = "00000000-0000-0001-0000-000000000000")
  val consumerId: UUID,
  @Schema(description = "Events Type", required = true, example = "DEATH_NOTIFICATION")
  val eventTypeId: String,
  @Schema(description = "Client ID used to poll event platform", required = false, example = "a-polling-client")
  val pollClientId: String? = null,
  @Schema(description = "Client ID used to callback to event platform", required = false, example = "a-callback-client")
  val callbackClientId: String? = null,
  @Schema(description = "URI where to push data, can be s3 or http", required = false, example = "http://localhost/callback")
  val pushUri: String? = null,
  @Schema(description = "NI number required in response", required = false, example = "true", defaultValue = "false")
  val ninoRequired: Boolean = false,
)
