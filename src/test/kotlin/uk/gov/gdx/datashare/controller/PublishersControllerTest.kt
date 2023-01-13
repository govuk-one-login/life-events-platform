package uk.gov.gdx.datashare.controller

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.repository.Publisher
import uk.gov.gdx.datashare.repository.PublisherSubscription
import uk.gov.gdx.datashare.service.PublisherRequest
import uk.gov.gdx.datashare.service.PublisherSubRequest
import uk.gov.gdx.datashare.service.PublishersService
import java.util.UUID

class PublishersControllerTest {
  private val publishersService = mockk<PublishersService>()

  private val underTest = PublishersController(publishersService)

  @Test
  fun `getPublishers gets publishers`() {
    runBlocking {
      val publishers = flowOf(
        Publisher(
          name = "Publisher 1",
        ),
        Publisher(
          name = "Publisher 2",
        ),
      )

      coEvery { publishersService.getPublishers() }.returns(publishers)

      val publishersOutput = underTest.getPublishers().toList()

      assertThat(publishersOutput).hasSize(2)
      assertThat(publishersOutput).isEqualTo(publishers.toList())
    }
  }

  @Test
  fun `addPublisher adds publisher`() {
    runBlocking {
      val publisherRequest = PublisherRequest(
        name = "Publisher",
      )
      val publisher = Publisher(name = publisherRequest.name)

      coEvery { publishersService.addPublisher(any()) }.returns(publisher)

      val publisherOutput = underTest.addPublisher(publisherRequest)

      assertThat(publisherOutput).isEqualTo(publisher)
    }
  }

  @Test
  fun `getPublisherSubscriptions gets publisher subscriptions`() {
    runBlocking {
      val publisherSubscriptions = flowOf(
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

      coEvery { publishersService.getPublisherSubscriptions() }.returns(publisherSubscriptions)

      val publisherSubscriptionsOutput = underTest.getPublisherSubscriptions().toList()

      assertThat(publisherSubscriptionsOutput).hasSize(3)
      assertThat(publisherSubscriptionsOutput).isEqualTo(publisherSubscriptions.toList())
    }
  }

  @Test
  fun `getSubscriptionsForPublisher gets publisher subscriptions`() {
    runBlocking {
      val publisherId = UUID.randomUUID()
      val publisherSubscriptions = flowOf(
        PublisherSubscription(
          publisherId = publisherId,
          clientId = "Client-1",
          eventTypeId = "DEATH_NOTIFICATION",
          datasetId = "LEV",
        ),
        PublisherSubscription(
          publisherId = publisherId,
          clientId = "Client-2",
          eventTypeId = "DEATH_NOTIFICATION",
          datasetId = "LEV",
        ),
        PublisherSubscription(
          publisherId = publisherId,
          clientId = "Client-3",
          eventTypeId = "DEATH_NOTIFICATION",
          datasetId = "LEV",
        ),
      )

      coEvery { publishersService.getSubscriptionsForPublisher(publisherId) }.returns(publisherSubscriptions)

      val publisherSubscriptionsOutput = underTest.getSubscriptionsForPublisher(publisherId).toList()

      assertThat(publisherSubscriptionsOutput).hasSize(3)
      assertThat(publisherSubscriptionsOutput).isEqualTo(publisherSubscriptions.toList())
    }
  }

  @Test
  fun `addPublisherSubscription adds publisher subscription`() {
    runBlocking {
      val publisherId = UUID.randomUUID()
      val publisherSubscriptionRequest = PublisherSubRequest(
        clientId = "Client-New",
        eventTypeId = "DEATH_NOTIFICATION_NEW",
        datasetId = "LEV_NEW",
      )
      val publisherSubscription = PublisherSubscription(
        publisherId = publisherId,
        clientId = "Client-New",
        eventTypeId = "DEATH_NOTIFICATION_NEW",
        datasetId = "LEV_NEW",
      )

      coEvery { publishersService.addPublisherSubscription(publisherId, any()) }.returns(publisherSubscription)

      val publisherSubscriptionOutput = underTest.addPublisherSubscription(publisherId, publisherSubscriptionRequest)

      assertThat(publisherSubscriptionOutput).isEqualTo(publisherSubscription)
    }
  }

  @Test
  fun `updatePublisherSubscription updates publisher subscription`() {
    runBlocking {
      val publisherId = UUID.randomUUID()
      val subscriptionId = UUID.randomUUID()
      val publisherSubscriptionRequest = PublisherSubRequest(
        clientId = "Client-New",
        eventTypeId = "DEATH_NOTIFICATION_NEW",
        datasetId = "LEV_NEW",
      )
      val publisherSubscription = PublisherSubscription(
        publisherId = publisherId,
        publisherSubscriptionId = subscriptionId,
        clientId = "Client-New",
        eventTypeId = "DEATH_NOTIFICATION_NEW",
        datasetId = "LEV_NEW",
      )

      coEvery { publishersService.updatePublisherSubscription(publisherId, subscriptionId, any()) }.returns(publisherSubscription)

      val publisherSubscriptionOutput = underTest.updatePublisherSubscription(publisherId, subscriptionId, publisherSubscriptionRequest)

      assertThat(publisherSubscriptionOutput).isEqualTo(publisherSubscription)
    }
  }
}
