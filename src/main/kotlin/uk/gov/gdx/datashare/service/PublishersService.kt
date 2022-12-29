package uk.gov.gdx.datashare.service

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.gdx.datashare.repository.EventPublisher
import uk.gov.gdx.datashare.repository.EventPublisherRepository
import uk.gov.gdx.datashare.repository.EventSubscription
import uk.gov.gdx.datashare.repository.EventSubscriptionRepository
import java.util.*

@Service
@Transactional
class PublishersService(
  private val eventSubscriptionRepository: EventSubscriptionRepository,
  private val eventPublisherRepository: EventPublisherRepository,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun getPublishers() = eventPublisherRepository.findAll()

  suspend fun getPublisherSubscriptions() = eventSubscriptionRepository.findAll()

  suspend fun getSubscriptionsForPublisher(publisherId: UUID) = eventSubscriptionRepository.findAllByPublisherId(publisherId)

  suspend fun addPublisherSubscription(
    publisherId: UUID,
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

  suspend fun updatePublisherSubscription(
    publisherId: UUID,
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

  suspend fun addPublisher(
    publisherRequest: PublisherRequest
  ): EventPublisher {
    with(publisherRequest) {
      return eventPublisherRepository.save(
        EventPublisher(
          publisherName = name
        )
      )
    }
  }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Publisher Subscription Request")
data class PublisherSubRequest(
  @Schema(description = "Client ID", required = true, example = "a-client-id")
  val clientId: String,
  @Schema(description = "Events Type", required = true, example = "DEATH_NOTIFICATION")
  val eventTypeId: String,
  @Schema(description = "Data Set", required = true, example = "DEATH_LEN")
  val datasetId: String
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Publisher Request")
data class PublisherRequest(
  @Schema(description = "Publisher name", required = true, example = "DWP")
  val name: String,
)