package uk.gov.gdx.datashare.resource

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.repository.*
import uk.gov.gdx.datashare.service.PublisherRequest
import uk.gov.gdx.datashare.service.PublisherSubRequest
import uk.gov.gdx.datashare.service.PublishersService
import java.util.*

class PublishersControllerTest {
  private val publishersService = mockk<PublishersService>()
  
  private val underTest = PublishersController(publishersService)
  
  @Test
  fun `getPublishers gets publishers`(){
    runBlocking { 
      val publishersInput = flowOf(
        Publisher(
          name = "Publisher 1"
        ),
        Publisher(
          name = "Publisher 2"
        ),
      )
      
      coEvery { publishersService.getPublishers() }.returns(publishersInput)
      
      val publishers = underTest.getPublishers().toList()
      
      assertThat(publishers).hasSize(2)
      assertThat(publishers).isEqualTo(publishersInput.toList())
    }
  }

  @Test
  fun `addPublisher adds publisher`(){
    runBlocking {
      val publisherRequest = PublisherRequest(
        name = "Publisher"
      )
      val publisherInput = Publisher(name = publisherRequest.name)

      coEvery { publishersService.addPublisher(any()) }.returns(publisherInput)

      val publisher = underTest.addPublisher(publisherRequest)

      assertThat(publisher).isEqualTo(publisherInput)
    }
  }
  
  @Test
  fun `getPublisherSubscriptions gets publisher subscriptions`(){
    runBlocking {
      val publisherSubscriptionsInput = flowOf(
        PublisherSubscription(
          publisherId = UUID.randomUUID(),
          clientId = "Client-1",
          eventTypeId = "DEATH_NOTIFICATION",
          datasetId = "LEV"
        ),
        PublisherSubscription(
          publisherId = UUID.randomUUID(),
          clientId = "Client-2",
          eventTypeId = "DEATH_NOTIFICATION",
          datasetId = "LEV"
        ),
        PublisherSubscription(
          publisherId = UUID.randomUUID(),
          clientId = "Client-3",
          eventTypeId = "DEATH_NOTIFICATION",
          datasetId = "LEV"
        ),
      )

      coEvery { publishersService.getPublisherSubscriptions() }.returns(publisherSubscriptionsInput)

      val publisherSubscriptions = underTest.getPublisherSubscriptions().toList()

      assertThat(publisherSubscriptions).hasSize(3)
      assertThat(publisherSubscriptions).isEqualTo(publisherSubscriptionsInput.toList())
    }
  }

  @Test
  fun `addPublisherSubscription adds publisher subscription`(){
    runBlocking {
      val publisherId = UUID.randomUUID()
      val publisherSubscriptionRequest = PublisherSubRequest(
        clientId = "Client-New",
        eventTypeId = "DEATH_NOTIFICATION_NEW",
        datasetId = "LEV_NEW"
      )
      val publisherSubscriptionInput = PublisherSubscription(
        publisherId = publisherId,
        clientId = "Client-New",
        eventTypeId = "DEATH_NOTIFICATION_NEW",
        datasetId = "LEV_NEW"
      )

      coEvery { publishersService.addPublisherSubscription(publisherId, any()) }.returns(publisherSubscriptionInput)

      val publisherSubscription = underTest.addPublisherSubscription(publisherId, publisherSubscriptionRequest)

      assertThat(publisherSubscription).isEqualTo(publisherSubscriptionInput)
    }
  }

  @Test
  fun `updatePublisherSubscription updates publisher subscription`(){
    runBlocking {
      val publisherId = UUID.randomUUID()
      val subscriptionId = UUID.randomUUID()
      val publisherSubscriptionRequest = PublisherSubRequest(
        clientId = "Client-New",
        eventTypeId = "DEATH_NOTIFICATION_NEW",
        datasetId = "LEV_NEW"
      )
      val publisherSubscriptionInput = PublisherSubscription(
        publisherId = publisherId,
        publisherSubscriptionId = subscriptionId,
        clientId = "Client-New",
        eventTypeId = "DEATH_NOTIFICATION_NEW",
        datasetId = "LEV_NEW"
      )

      coEvery { publishersService.updatePublisherSubscription(publisherId, subscriptionId, any()) }.returns(publisherSubscriptionInput)

      val publisherSubscription = underTest.updatePublisherSubscription(publisherId, subscriptionId, publisherSubscriptionRequest)

      assertThat(publisherSubscription).isEqualTo(publisherSubscriptionInput)
    }
  }
}