package uk.gov.gdx.datashare.controller

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.repository.Consumer
import uk.gov.gdx.datashare.repository.ConsumerSubscription
import uk.gov.gdx.datashare.service.ConsumerRequest
import uk.gov.gdx.datashare.service.ConsumerSubRequest
import uk.gov.gdx.datashare.service.ConsumersService
import java.util.*

class ConsumersControllerTest {
  private val consumersService = mockk<ConsumersService>()

  private val underTest = ConsumersController(consumersService)

  @Test
  fun `getConsumers gets consumers`() {
    val consumers = listOf(
      Consumer(
        name = "Consumer 1",
      ),
      Consumer(
        name = "Consumer 2",
      ),
    )

    every { consumersService.getConsumers() }.returns(consumers)

    val consumersOutput = underTest.getConsumers().toList()

    assertThat(consumersOutput).hasSize(2)
    assertThat(consumersOutput).isEqualTo(consumers.toList())
  }

  @Test
  fun `addConsumer adds consumer`() {
    val consumerRequest = ConsumerRequest(
      name = "Consumer",
    )
    val consumer = Consumer(name = consumerRequest.name)

    every { consumersService.addConsumer(any()) }.returns(consumer)

    val consumerOutput = underTest.addConsumer(consumerRequest)

    assertThat(consumerOutput).isEqualTo(consumer)
  }

  @Test
  fun `getConsumerSubscriptions gets consumer subscriptions`() {
    val consumerSubscriptions = listOf(
      ConsumerSubscription(
        consumerId = UUID.randomUUID(),
        eventType = EventType.DEATH_NOTIFICATION,
        enrichmentFields = "a,b,c",
      ),
      ConsumerSubscription(
        consumerId = UUID.randomUUID(),
        eventType = EventType.DEATH_NOTIFICATION,
        enrichmentFields = "a,b,c",
      ),
      ConsumerSubscription(
        consumerId = UUID.randomUUID(),
        eventType = EventType.DEATH_NOTIFICATION,
        enrichmentFields = "a,b,c",
      ),
    )

    every { consumersService.getConsumerSubscriptions() }.returns(consumerSubscriptions)

    val consumerSubscriptionsOutput = underTest.getConsumerSubscriptions().toList()

    assertThat(consumerSubscriptionsOutput).hasSize(3)
    assertThat(consumerSubscriptionsOutput).isEqualTo(consumerSubscriptions.toList())
  }

  @Test
  fun `getSubscriptionsForConsumer gets consumer subscriptions`() {
    val consumerId = UUID.randomUUID()
    val consumerSubscriptions = listOf(
      ConsumerSubscription(
        consumerId = consumerId,
        eventType = EventType.DEATH_NOTIFICATION,
        enrichmentFields = "a,b,c",
      ),
      ConsumerSubscription(
        consumerId = consumerId,
        eventType = EventType.DEATH_NOTIFICATION,
        enrichmentFields = "a,b,c",
      ),
      ConsumerSubscription(
        consumerId = consumerId,
        eventType = EventType.DEATH_NOTIFICATION,
        enrichmentFields = "a,b,c",
      ),
    )

    every { consumersService.getSubscriptionsForConsumer(consumerId) }.returns(consumerSubscriptions)

    val consumerSubscriptionsOutput = underTest.getSubscriptionsForConsumer(consumerId).toList()

    assertThat(consumerSubscriptionsOutput).hasSize(3)
    assertThat(consumerSubscriptionsOutput).isEqualTo(consumerSubscriptions.toList())
  }

  @Test
  fun `addConsumerSubscription adds consumer subscription`() {
    val consumerId = UUID.randomUUID()
    val consumerSubscriptionRequest = ConsumerSubRequest(
      eventType = EventType.LIFE_EVENT,
      enrichmentFields = "a,b,c,New",
    )
    val consumerSubscription = ConsumerSubscription(
      consumerId = consumerId,
      eventType = EventType.LIFE_EVENT,
      enrichmentFields = "a,b,c,New",
    )

    every { consumersService.addConsumerSubscription(consumerId, any()) }.returns(consumerSubscription)

    val consumerSubscriptionOutput = underTest.addConsumerSubscription(consumerId, consumerSubscriptionRequest)

    assertThat(consumerSubscriptionOutput).isEqualTo(consumerSubscription)
  }

  @Test
  fun `updateConsumerSubscription updates consumer subscription`() {
    val consumerId = UUID.randomUUID()
    val subscriptionId = UUID.randomUUID()
    val consumerSubscriptionRequest = ConsumerSubRequest(
      eventType = EventType.LIFE_EVENT,
      enrichmentFields = "a,b,c,New",
    )
    val consumerSubscription = ConsumerSubscription(
      consumerId = consumerId,
      consumerSubscriptionId = subscriptionId,
      oauthClientId = "callbackClientId",
      eventType = EventType.LIFE_EVENT,
      enrichmentFields = "a,b,c,New",
    )

    every { consumersService.updateConsumerSubscription(consumerId, subscriptionId, any()) }.returns(
      consumerSubscription,
    )

    val consumerSubscriptionOutput =
      underTest.updateConsumerSubscription(consumerId, subscriptionId, consumerSubscriptionRequest)

    assertThat(consumerSubscriptionOutput).isEqualTo(consumerSubscription)
  }
}
