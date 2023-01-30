package uk.gov.gdx.datashare.services

import com.amazonaws.xray.spring.aop.XRayEnabled
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.gdx.datashare.config.PublisherSubscriptionNotFoundException
import uk.gov.gdx.datashare.models.PublisherRequest
import uk.gov.gdx.datashare.models.PublisherSubRequest
import uk.gov.gdx.datashare.repositories.Publisher
import uk.gov.gdx.datashare.repositories.PublisherRepository
import uk.gov.gdx.datashare.repositories.PublisherSubscription
import uk.gov.gdx.datashare.repositories.PublisherSubscriptionRepository
import java.util.*

@Service
@Transactional
@XRayEnabled
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
