package uk.gov.gdx.datashare.controllers

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.models.PublisherRequest
import uk.gov.gdx.datashare.models.PublisherSubRequest
import uk.gov.gdx.datashare.repositories.Publisher
import uk.gov.gdx.datashare.repositories.PublisherSubscription
import uk.gov.gdx.datashare.services.PublishersService
import java.util.*

class PublishersControllerTest {
  private val publishersService = mockk<PublishersService>()

  private val underTest = PublishersController(publishersService)

  @Test
  fun `getPublishers gets publishers`() {
    val publishers = listOf(
      Publisher(
        name = "Publisher 1",
      ),
      Publisher(
        name = "Publisher 2",
      ),
    )

    every { publishersService.getPublishers() }.returns(publishers)

    val publishersOutput = underTest.getPublishers().toList()

    assertThat(publishersOutput).hasSize(2)
    assertThat(publishersOutput).isEqualTo(publishers.toList())
  }

  @Test
  fun `addPublisher adds publisher`() {
    val publisherRequest = PublisherRequest(
      name = "Publisher",
    )
    val publisher = Publisher(name = publisherRequest.name)

    every { publishersService.addPublisher(any()) }.returns(publisher)

    val publisherOutput = underTest.addPublisher(publisherRequest)

    assertThat(publisherOutput).isEqualTo(publisher)
  }

  @Test
  fun `getPublisherSubscriptions gets publisher subscriptions`() {
    val publisherSubscriptions = listOf(
      PublisherSubscription(
        publisherId = UUID.randomUUID(),
        clientId = "Client-1",
        eventType = EventType.DEATH_NOTIFICATION,
      ),
      PublisherSubscription(
        publisherId = UUID.randomUUID(),
        clientId = "Client-2",
        eventType = EventType.DEATH_NOTIFICATION,
      ),
      PublisherSubscription(
        publisherId = UUID.randomUUID(),
        clientId = "Client-3",
        eventType = EventType.DEATH_NOTIFICATION,
      ),
    )

    every { publishersService.getPublisherSubscriptions() }.returns(publisherSubscriptions)

    val publisherSubscriptionsOutput = underTest.getPublisherSubscriptions().toList()

    assertThat(publisherSubscriptionsOutput).hasSize(3)
    assertThat(publisherSubscriptionsOutput).isEqualTo(publisherSubscriptions.toList())
  }

  @Test
  fun `getSubscriptionsForPublisher gets publisher subscriptions`() {
    val publisherId = UUID.randomUUID()
    val publisherSubscriptions = listOf(
      PublisherSubscription(
        publisherId = publisherId,
        clientId = "Client-1",
        eventType = EventType.DEATH_NOTIFICATION,
      ),
      PublisherSubscription(
        publisherId = publisherId,
        clientId = "Client-2",
        eventType = EventType.DEATH_NOTIFICATION,
      ),
      PublisherSubscription(
        publisherId = publisherId,
        clientId = "Client-3",
        eventType = EventType.DEATH_NOTIFICATION,
      ),
    )

    every { publishersService.getSubscriptionsForPublisher(publisherId) }.returns(publisherSubscriptions)

    val publisherSubscriptionsOutput = underTest.getSubscriptionsForPublisher(publisherId).toList()

    assertThat(publisherSubscriptionsOutput).hasSize(3)
    assertThat(publisherSubscriptionsOutput).isEqualTo(publisherSubscriptions.toList())
  }

  @Test
  fun `addPublisherSubscription adds publisher subscription`() {
    val publisherId = UUID.randomUUID()
    val publisherSubscriptionRequest = PublisherSubRequest(
      clientId = "Client-New",
      eventType = EventType.LIFE_EVENT,
    )
    val publisherSubscription = PublisherSubscription(
      publisherId = publisherId,
      clientId = "Client-New",
      eventType = EventType.LIFE_EVENT,
    )

    every { publishersService.addPublisherSubscription(publisherId, any()) }.returns(publisherSubscription)

    val publisherSubscriptionOutput = underTest.addPublisherSubscription(publisherId, publisherSubscriptionRequest)

    assertThat(publisherSubscriptionOutput).isEqualTo(publisherSubscription)
  }

  @Test
  fun `updatePublisherSubscription updates publisher subscription`() {
    val publisherId = UUID.randomUUID()
    val subscriptionId = UUID.randomUUID()
    val publisherSubscriptionRequest = PublisherSubRequest(
      clientId = "Client-New",
      eventType = EventType.LIFE_EVENT,
    )
    val publisherSubscription = PublisherSubscription(
      publisherId = publisherId,
      publisherSubscriptionId = subscriptionId,
      clientId = "Client-New",
      eventType = EventType.LIFE_EVENT,
    )

    every { publishersService.updatePublisherSubscription(publisherId, subscriptionId, any()) }.returns(
      publisherSubscription,
    )

    val publisherSubscriptionOutput =
      underTest.updatePublisherSubscription(publisherId, subscriptionId, publisherSubscriptionRequest)

    assertThat(publisherSubscriptionOutput).isEqualTo(publisherSubscription)
  }
}
