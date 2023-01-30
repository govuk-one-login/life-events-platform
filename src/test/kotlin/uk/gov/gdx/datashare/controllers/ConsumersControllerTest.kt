package uk.gov.gdx.datashare.controllers

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.models.ConsumerRequest
import uk.gov.gdx.datashare.models.ConsumerSubRequest
import uk.gov.gdx.datashare.models.ConsumerSubscriptionDto
import uk.gov.gdx.datashare.repositories.Consumer
import uk.gov.gdx.datashare.services.ConsumersService
import java.time.LocalDateTime
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
    val consumerSubscriptionDtos = listOf(
      ConsumerSubscriptionDto(
        consumerSubscriptionId = UUID.randomUUID(),
        consumerId = UUID.randomUUID(),
        eventType = EventType.DEATH_NOTIFICATION,
        enrichmentFields = listOf("a"),
        enrichmentFieldsIncludedInPoll = false,
        whenCreated = LocalDateTime.now(),
      ),
      ConsumerSubscriptionDto(
        consumerSubscriptionId = UUID.randomUUID(),
        consumerId = UUID.randomUUID(),
        eventType = EventType.DEATH_NOTIFICATION,
        enrichmentFields = listOf("a"),
        enrichmentFieldsIncludedInPoll = false,
        whenCreated = LocalDateTime.now(),
      ),
      ConsumerSubscriptionDto(
        consumerSubscriptionId = UUID.randomUUID(),
        consumerId = UUID.randomUUID(),
        eventType = EventType.DEATH_NOTIFICATION,
        enrichmentFields = listOf("a"),
        enrichmentFieldsIncludedInPoll = false,
        whenCreated = LocalDateTime.now(),
      ),
    )

    every { consumersService.getConsumerSubscriptions() }.returns(consumerSubscriptionDtos)

    val consumerSubscriptionsOutput = underTest.getConsumerSubscriptions().toList()

    assertThat(consumerSubscriptionsOutput).hasSize(3)
    assertThat(consumerSubscriptionsOutput).isEqualTo(consumerSubscriptionDtos.toList())
  }

  @Test
  fun `getSubscriptionsForConsumer gets consumer subscriptions`() {
    val consumerId = UUID.randomUUID()
    val consumerSubscriptionDtos = listOf(
      ConsumerSubscriptionDto(
        consumerSubscriptionId = UUID.randomUUID(),
        consumerId = consumerId,
        eventType = EventType.DEATH_NOTIFICATION,
        enrichmentFields = listOf("a"),
        enrichmentFieldsIncludedInPoll = false,
        whenCreated = LocalDateTime.now(),
      ),
      ConsumerSubscriptionDto(
        consumerSubscriptionId = UUID.randomUUID(),
        consumerId = consumerId,
        eventType = EventType.DEATH_NOTIFICATION,
        enrichmentFields = listOf("a"),
        enrichmentFieldsIncludedInPoll = false,
        whenCreated = LocalDateTime.now(),
      ),
      ConsumerSubscriptionDto(
        consumerSubscriptionId = UUID.randomUUID(),
        consumerId = consumerId,
        eventType = EventType.DEATH_NOTIFICATION,
        enrichmentFields = listOf("a"),
        enrichmentFieldsIncludedInPoll = false,
        whenCreated = LocalDateTime.now(),
      ),
    )

    every { consumersService.getSubscriptionsForConsumer(consumerId) }.returns(consumerSubscriptionDtos)

    val consumerSubscriptionsOutput = underTest.getSubscriptionsForConsumer(consumerId).toList()

    assertThat(consumerSubscriptionsOutput).hasSize(3)
    assertThat(consumerSubscriptionsOutput).isEqualTo(consumerSubscriptionDtos.toList())
  }

  @Test
  fun `addConsumerSubscription adds consumer subscription`() {
    val consumerId = UUID.randomUUID()
    val consumerSubscriptionRequest = ConsumerSubRequest(
      eventType = EventType.LIFE_EVENT,
      enrichmentFields = listOf("a", "b", "c", "New"),
    )
    val consumerSubscriptionDto = ConsumerSubscriptionDto(
      consumerSubscriptionId = UUID.randomUUID(),
      consumerId = consumerId,
      eventType = EventType.LIFE_EVENT,
      enrichmentFields = listOf("a", "b", "c", "New"),
      enrichmentFieldsIncludedInPoll = false,
      whenCreated = LocalDateTime.now(),
    )

    every { consumersService.addConsumerSubscription(consumerId, any()) }.returns(consumerSubscriptionDto)

    val consumerSubscriptionOutput = underTest.addConsumerSubscription(consumerId, consumerSubscriptionRequest)

    assertThat(consumerSubscriptionOutput).isEqualTo(consumerSubscriptionDto)
  }

  @Test
  fun `updateConsumerSubscription updates consumer subscription`() {
    val consumerId = UUID.randomUUID()
    val subscriptionId = UUID.randomUUID()
    val consumerSubscriptionRequest = ConsumerSubRequest(
      eventType = EventType.LIFE_EVENT,
      enrichmentFields = listOf("a", "b", "c", "New"),
    )
    val consumerSubscriptionDto = ConsumerSubscriptionDto(
      consumerId = consumerId,
      consumerSubscriptionId = subscriptionId,
      oauthClientId = "callbackClientId",
      eventType = EventType.LIFE_EVENT,
      enrichmentFields = listOf("a", "b", "c", "New"),
      enrichmentFieldsIncludedInPoll = false,
      whenCreated = LocalDateTime.now(),
    )

    every { consumersService.updateConsumerSubscription(consumerId, subscriptionId, any()) }.returns(
      consumerSubscriptionDto,
    )

    val consumerSubscriptionOutput =
      underTest.updateConsumerSubscription(consumerId, subscriptionId, consumerSubscriptionRequest)

    assertThat(consumerSubscriptionOutput).isEqualTo(consumerSubscriptionDto)
  }
}
