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
      val consumersInput = flowOf(
        Consumer(
          name = "Consumer 1"
        ),
        Consumer(
          name = "Consumer 2"
        ),
      )
      
      coEvery { consumersService.getConsumers() }.returns(consumersInput)
      
      val consumers = underTest.getConsumers().toList()
      
      assertThat(consumers).hasSize(2)
      assertThat(consumers).isEqualTo(consumersInput.toList())
    }
  }

  @Test
  fun `addConsumer adds consumer`(){
    runBlocking {
      val consumerRequest = ConsumerRequest(
        name = "Consumer"
      )
      val consumerInput = Consumer(name = consumerRequest.name)

      coEvery { consumersService.addConsumer(any()) }.returns(consumerInput)

      val consumer = underTest.addConsumer(consumerRequest)

      assertThat(consumer).isEqualTo(consumerInput)
    }
  }
  
  @Test
  fun `getConsumerSubscriptions gets consumer subscriptions`(){
    runBlocking {
      val consumerSubscriptionsInput = flowOf(
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

      coEvery { consumersService.getConsumerSubscriptions() }.returns(consumerSubscriptionsInput)

      val consumerSubscriptions = underTest.getConsumerSubscriptions().toList()

      assertThat(consumerSubscriptions).hasSize(3)
      assertThat(consumerSubscriptions).isEqualTo(consumerSubscriptionsInput.toList())
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
      val consumerSubscriptionInput = ConsumerSubscription(
        consumerId = consumerId,
        callbackClientId = "callbackClientId",
        ingressEventType = "DEATH_NOTIFICATIONNew",
        enrichmentFields = "a,b,c,New",
      )

      coEvery { consumersService.addConsumerSubscription(consumerId, any()) }.returns(consumerSubscriptionInput)

      val consumerSubscription = underTest.addConsumerSubscription(consumerId, consumerSubscriptionRequest)

      assertThat(consumerSubscription).isEqualTo(consumerSubscriptionInput)
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
      val consumerSubscriptionInput = ConsumerSubscription(
        consumerId = consumerId,
        consumerSubscriptionId = subscriptionId,
        callbackClientId = "callbackClientId",
        ingressEventType = "DEATH_NOTIFICATIONNew",
        enrichmentFields = "a,b,c,New",
      )

      coEvery { consumersService.updateConsumerSubscription(consumerId, subscriptionId, any()) }.returns(consumerSubscriptionInput)

      val consumerSubscription = underTest.updateConsumerSubscription(consumerId, subscriptionId, consumerSubscriptionRequest)

      assertThat(consumerSubscription).isEqualTo(consumerSubscriptionInput)
    }
  }
}