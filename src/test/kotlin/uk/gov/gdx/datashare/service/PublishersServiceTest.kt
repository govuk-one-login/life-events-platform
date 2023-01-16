package uk.gov.gdx.datashare.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.gdx.datashare.config.PublisherSubscriptionNotFoundException
import uk.gov.gdx.datashare.repository.Publisher
import uk.gov.gdx.datashare.repository.PublisherRepository
import uk.gov.gdx.datashare.repository.PublisherSubscription
import uk.gov.gdx.datashare.repository.PublisherSubscriptionRepository
import java.util.*

class PublishersServiceTest {
  private val publisherSubscriptionRepository = mockk<PublisherSubscriptionRepository>()
  private val publisherRepository = mockk<PublisherRepository>()

  private val underTest = PublishersService(publisherSubscriptionRepository, publisherRepository)

  @Test
  fun `getPublishers gets all publishers`() {
    runBlocking {
      val savedPublishers = flowOf(
        Publisher(name = "Publisher1"),
        Publisher(name = "Publisher2"),
        Publisher(name = "Publisher3"),
      )

      coEvery { publisherRepository.findAll() }.returns(savedPublishers)

      val publishers = underTest.getPublishers().toList()

      assertThat(publishers).hasSize(3)
      assertThat(publishers).isEqualTo(savedPublishers.toList())
    }
  }

  @Test
  fun `getPublisherSubscriptions gets all publisher subscriptions`() {
    runBlocking {
      val savedPublisherSubscriptions = flowOf(
        PublisherSubscription(
          publisherId = UUID.randomUUID(),
          clientId = "Client-1",
          eventTypeId = "DEATH_NOTIFICATION",
          datasetId = "LEV",
        ),
        PublisherSubscription(
          publisherId = UUID.randomUUID(),
          clientId = "Client-2",
          eventTypeId = "DEATH_NOTIFICATION",
          datasetId = "LEV",
        ),
        PublisherSubscription(
          publisherId = UUID.randomUUID(),
          clientId = "Client-3",
          eventTypeId = "DEATH_NOTIFICATION",
          datasetId = "LEV",
        ),
      )

      coEvery { publisherSubscriptionRepository.findAll() }.returns(savedPublisherSubscriptions)

      val publisherSubscriptions = underTest.getPublisherSubscriptions().toList()

      assertThat(publisherSubscriptions).hasSize(3)
      assertThat(publisherSubscriptions).isEqualTo(savedPublisherSubscriptions.toList())
    }
  }

  @Test
  fun `getSubscriptionsForPublisher gets all publisher subscriptions for id`() {
    runBlocking {
      val savedPublisherSubscriptions = flowOf(
        PublisherSubscription(
          publisherId = publisher.id,
          clientId = "Client-1",
          eventTypeId = "DEATH_NOTIFICATION",
          datasetId = "LEV",
        ),
        PublisherSubscription(
          publisherId = publisher.id,
          clientId = "Client-2",
          eventTypeId = "DEATH_NOTIFICATION",
          datasetId = "LEV",
        ),
        PublisherSubscription(
          publisherId = publisher.id,
          clientId = "Client-3",
          eventTypeId = "DEATH_NOTIFICATION",
          datasetId = "LEV",
        ),
      )

      coEvery { publisherSubscriptionRepository.findAllByPublisherId(publisher.id) }.returns(savedPublisherSubscriptions)

      val publisherSubscriptions = underTest.getSubscriptionsForPublisher(publisher.id).toList()

      assertThat(publisherSubscriptions).hasSize(3)
      assertThat(publisherSubscriptions).isEqualTo(savedPublisherSubscriptions.toList())
    }
  }

  @Test
  fun `addPublisherSubscription adds new subscription if publisher exists`() {
    runBlocking {
      coEvery { publisherRepository.findById(publisher.id) }.returns(publisher)
      coEvery { publisherSubscriptionRepository.save(any()) }.returns(publisherSubscription)

      underTest.addPublisherSubscription(publisher.id, publisherSubRequest)

      coVerify(exactly = 1) {
        publisherSubscriptionRepository.save(
          withArg {
            assertThat(it.publisherId).isEqualTo(publisher.id)
            assertThat(it.clientId).isEqualTo(publisherSubRequest.clientId)
            assertThat(it.eventTypeId).isEqualTo(publisherSubRequest.eventTypeId)
            assertThat(it.datasetId).isEqualTo(publisherSubRequest.datasetId)
          },
        )
      }
    }
  }

  @Test
  fun `updatePublisherSubscription updates subscription`() {
    runBlocking {
      coEvery { publisherRepository.findById(publisher.id) }.returns(publisher)
      coEvery { publisherSubscriptionRepository.findById(publisherSubscription.id) }.returns(publisherSubscription)

      coEvery { publisherSubscriptionRepository.save(any()) }.returns(publisherSubscription)

      underTest.updatePublisherSubscription(publisher.id, publisherSubscription.id, publisherSubRequest)

      coVerify(exactly = 1) {
        publisherSubscriptionRepository.save(
          withArg {
            assertThat(it.publisherId).isEqualTo(publisher.id)
            assertThat(it.clientId).isEqualTo(publisherSubRequest.clientId)
            assertThat(it.eventTypeId).isEqualTo(publisherSubRequest.eventTypeId)
            assertThat(it.datasetId).isEqualTo(publisherSubRequest.datasetId)
          },
        )
      }
    }
  }

  @Test
  fun `updatePublisherSubscription does not update subscription if subscription does not exist`() {
    runBlocking {
      coEvery { publisherSubscriptionRepository.findById(publisherSubscription.id) }.returns(null)

      val exception = assertThrows<PublisherSubscriptionNotFoundException> {
        underTest.updatePublisherSubscription(publisher.id, publisherSubscription.id, publisherSubRequest)
      }

      assertThat(exception.message).isEqualTo("Subscription ${publisherSubscription.id} not found")

      coVerify(exactly = 0) { publisherSubscriptionRepository.save(any()) }
    }
  }

  @Test
  fun `addPublisher adds publisher`() {
    runBlocking {
      val publisherRequest = PublisherRequest(
        name = "Publisher",
      )

      coEvery { publisherRepository.save(any()) }.returns(publisher)

      underTest.addPublisher(publisherRequest)

      coVerify(exactly = 1) {
        publisherRepository.save(
          withArg {
            assertThat(it.name).isEqualTo(publisherRequest.name)
          },
        )
      }
    }
  }

  private val publisher = Publisher(name = "Base Publisher")
  private val publisherSubscription = PublisherSubscription(
    publisherId = publisher.id,
    clientId = "Client",
    eventTypeId = "DEATH_NOTIFICATION",
    datasetId = "LEV",
  )
  private val publisherSubRequest = PublisherSubRequest(
    clientId = "Client-New",
    eventTypeId = "DEATH_NOTIFICATION_NEW",
    datasetId = "LEV_NEW",
  )
}
