package uk.gov.gdx.datashare.service

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.gdx.datashare.config.PublisherSubscriptionNotFoundException
import uk.gov.gdx.datashare.enums.EventType
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

  fun getPublishers(): Iterable<Publisher> = publisherRepository.findAll()

  fun getPublisherSubscriptions(): Iterable<PublisherSubscription> = publisherSubscriptionRepository.findAll()

  fun getSubscriptionsForPublisher(publisherId: UUID) =
    publisherSubscriptionRepository.findAllByPublisherId(publisherId)

  fun addPublisherSubscription(
    publisherId: UUID,
    publisherSubRequest: PublisherSubRequest,
  ): PublisherSubscription {
    with(publisherSubRequest) {
      return publisherSubscriptionRepository.save(
        PublisherSubscription(
          publisherId = publisherId,
          clientId = clientId,
          eventType = eventType,
        ),
      )
    }
  }

  fun updatePublisherSubscription(
    publisherId: UUID,
    subscriptionId: UUID,
    publisherSubRequest: PublisherSubRequest,
  ): PublisherSubscription {
    with(publisherSubRequest) {
      return publisherSubscriptionRepository.save(
        publisherSubscriptionRepository.findByIdOrNull(subscriptionId)?.copy(
          publisherId = publisherId,
          clientId = clientId,
          eventType = eventType,
        ) ?: throw PublisherSubscriptionNotFoundException("Subscription $subscriptionId not found"),
      )
    }
  }

  fun addPublisher(
    publisherRequest: PublisherRequest,
  ): Publisher {
    with(publisherRequest) {
      return publisherRepository.save(
        Publisher(
          name = name,
        ),
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
  val eventType: EventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Publisher Request")
data class PublisherRequest(
  @Schema(description = "Publisher name", required = true, example = "DWP")
  val name: String,
)
