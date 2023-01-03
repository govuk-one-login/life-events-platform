package uk.gov.gdx.datashare.resource

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.repository.*
import uk.gov.gdx.datashare.service.ConsumerRequest
import uk.gov.gdx.datashare.service.ConsumerSubRequest
import uk.gov.gdx.datashare.service.ConsumersService
import java.util.*

class ConsumersControllerTest {
  private val consumersService = mockk<ConsumersService>()
  
  private val underTest = ConsumersController(consumersService)
  
  @Test
  fun `getConsumers gets consumers`(){
    runBlocking { 
      val consumers = flowOf(
        Consumer(
          name = "Consumer 1"
        ),
        Consumer(
          name = "Consumer 2"
        ),
      )
      
      coEvery { consumersService.getConsumers() }.returns(consumers)
      
      val consumersOutput = underTest.getConsumers().toList()
      
      assertThat(consumersOutput).hasSize(2)
      assertThat(consumersOutput).isEqualTo(consumers.toList())
    }
  }

  @Test
  fun `addConsumer adds consumer`(){
    runBlocking {
      val consumerRequest = ConsumerRequest(
        name = "Consumer"
      )
      val consumer = Consumer(name = consumerRequest.name)

      coEvery { consumersService.addConsumer(any()) }.returns(consumer)

      val consumerOutput = underTest.addConsumer(consumerRequest)

      assertThat(consumerOutput).isEqualTo(consumer)
    }
  }
  
  @Test
  fun `getConsumerSubscriptions gets consumer subscriptions`(){
    runBlocking {
      val consumerSubscriptions = flowOf(
        ConsumerSubscription(
          consumerId = UUID.randomUUID(),
          ingressEventType = "DEATH_NOTIFICATION",
          enrichmentFields = "a,b,c"
        ),
        ConsumerSubscription(
          consumerId = UUID.randomUUID(),
          ingressEventType = "DEATH_NOTIFICATION",
          enrichmentFields = "a,b,c"
        ),
        ConsumerSubscription(
          consumerId = UUID.randomUUID(),
          ingressEventType = "DEATH_NOTIFICATION",
          enrichmentFields = "a,b,c"
        ),
      )

      coEvery { consumersService.getConsumerSubscriptions() }.returns(consumerSubscriptions)

      val consumerSubscriptionsOutput = underTest.getConsumerSubscriptions().toList()

      assertThat(consumerSubscriptionsOutput).hasSize(3)
      assertThat(consumerSubscriptionsOutput).isEqualTo(consumerSubscriptions.toList())
    }
  }

  @Test
  fun `getSubscriptionsForConsumer gets consumer subscriptions`(){
    runBlocking {
      val consumerId = UUID.randomUUID()
      val consumerSubscriptions = flowOf(
        ConsumerSubscription(
          consumerId = consumerId,
          ingressEventType = "DEATH_NOTIFICATION",
          enrichmentFields = "a,b,c"
        ),
        ConsumerSubscription(
          consumerId = consumerId,
          ingressEventType = "DEATH_NOTIFICATION",
          enrichmentFields = "a,b,c"
        ),
        ConsumerSubscription(
          consumerId = consumerId,
          ingressEventType = "DEATH_NOTIFICATION",
          enrichmentFields = "a,b,c"
        ),
      )

      coEvery { consumersService.getSubscriptionsForConsumer(consumerId) }.returns(consumerSubscriptions)

      val consumerSubscriptionsOutput = underTest.getSubscriptionsForConsumer(consumerId).toList()

      assertThat(consumerSubscriptionsOutput).hasSize(3)
      assertThat(consumerSubscriptionsOutput).isEqualTo(consumerSubscriptions.toList())
    }
  }

  @Test
  fun `addConsumerSubscription adds consumer subscription`(){
    runBlocking {
      val consumerId = UUID.randomUUID()
      val consumerSubscriptionRequest = ConsumerSubRequest(
        ingressEventType = "DEATH_NOTIFICATIONNew",
        enrichmentFields = "a,b,c,New",
      )
      val consumerSubscription = ConsumerSubscription(
        consumerId = consumerId,
        callbackClientId = "callbackClientId",
        ingressEventType = "DEATH_NOTIFICATIONNew",
        enrichmentFields = "a,b,c,New",
      )

      coEvery { consumersService.addConsumerSubscription(consumerId, any()) }.returns(consumerSubscription)

      val consumerSubscriptionOutput = underTest.addConsumerSubscription(consumerId, consumerSubscriptionRequest)

      assertThat(consumerSubscriptionOutput).isEqualTo(consumerSubscription)
    }
  }

  @Test
  fun `updateConsumerSubscription updates consumer subscription`(){
    runBlocking {
      val consumerId = UUID.randomUUID()
      val subscriptionId = UUID.randomUUID()
      val consumerSubscriptionRequest = ConsumerSubRequest(
        ingressEventType = "DEATH_NOTIFICATIONNew",
        enrichmentFields = "a,b,c,New",
      )
      val consumerSubscription = ConsumerSubscription(
        consumerId = consumerId,
        consumerSubscriptionId = subscriptionId,
        callbackClientId = "callbackClientId",
        ingressEventType = "DEATH_NOTIFICATIONNew",
        enrichmentFields = "a,b,c,New",
      )

      coEvery { consumersService.updateConsumerSubscription(consumerId, subscriptionId, any()) }.returns(consumerSubscription)

      val consumerSubscriptionOutput = underTest.updateConsumerSubscription(consumerId, subscriptionId, consumerSubscriptionRequest)

      assertThat(consumerSubscriptionOutput).isEqualTo(consumerSubscription)
    }
  }
}