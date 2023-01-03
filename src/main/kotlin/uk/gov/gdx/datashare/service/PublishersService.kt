package uk.gov.gdx.datashare.service

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.gdx.datashare.repository.Publisher
import uk.gov.gdx.datashare.repository.PublisherRepository
import uk.gov.gdx.datashare.repository.PublisherSubscription
import uk.gov.gdx.datashare.repository.PublisherSubscriptionRepository
import java.util.*

@Service
@Transactional
class PublishersService(
  private val publisherSubscriptionRepository: PublisherSubscriptionRepository,
  private val publisherRepository: PublisherRepository,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun getPublishers() = publisherRepository.findAll()

  suspend fun getPublisherSubscriptions() = publisherSubscriptionRepository.findAll()

  suspend fun getSubscriptionsForPublisher(publisherId: UUID) =
    publisherSubscriptionRepository.findAllByPublisherId(publisherId)

  suspend fun addPublisherSubscription(
    publisherId: UUID,
    publisherSubRequest: PublisherSubRequest
  ): PublisherSubscription {
    with(publisherSubRequest) {
      return publisherSubscriptionRepository.save(
        PublisherSubscription(
          publisherId = publisherId,
          clientId = clientId,
          eventTypeId = eventTypeId,
          datasetId = datasetId,
        )
      )
    }
  }

  suspend fun updatePublisherSubscription(
    publisherId: UUID,
    subscriptionId: UUID,
    publisherSubRequest: PublisherSubRequest
  ): PublisherSubscription {
    with(publisherSubRequest) {
      return publisherSubscriptionRepository.save(
        publisherSubscriptionRepository.findById(subscriptionId)?.copy(
          publisherId = publisherId,
          clientId = clientId,
          eventTypeId = eventTypeId,
          datasetId = datasetId
        ) ?: throw RuntimeException("Subscription $subscriptionId not found")
      )
    }
  }

  suspend fun addPublisher(
    publisherRequest: PublisherRequest
  ): Publisher {
    with(publisherRequest) {
      return publisherRepository.save(
        Publisher(
          name = name
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
