package uk.gov.gdx.datashare.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import uk.gov.gdx.datashare.config.ConsumerSubscriptionNotFoundException
import uk.gov.gdx.datashare.repository.Consumer
import uk.gov.gdx.datashare.repository.ConsumerRepository
import uk.gov.gdx.datashare.repository.ConsumerSubscription
import uk.gov.gdx.datashare.repository.ConsumerSubscriptionRepository
import java.util.*

class ConsumersServiceTest {
  private val consumerSubscriptionRepository = mockk<ConsumerSubscriptionRepository>()
  private val consumerRepository = mockk<ConsumerRepository>()

  private val underTest = ConsumersService(
    consumerSubscriptionRepository,
    consumerRepository,
  )

  @Test
  fun `getConsumers gets all consumers`() {
    val savedConsumers = listOf(
      Consumer(name = "Consumer1"),
      Consumer(name = "Consumer2"),
      Consumer(name = "Consumer3"),
    )

    every { consumerRepository.findAll() }.returns(savedConsumers)

    val consumers = underTest.getConsumers().toList()

    assertThat(consumers).hasSize(3)
    assertThat(consumers).isEqualTo(savedConsumers.toList())
  }

  @Test
  fun `getConsumerSubscriptions gets all consumer subscriptions`() {
    val savedConsumerSubscriptions = listOf(
      ConsumerSubscription(
        consumerId = UUID.randomUUID(),
        eventType = "DEATH_NOTIFICATION",
        enrichmentFields = "a,b,c",
      ),
      ConsumerSubscription(
        consumerId = UUID.randomUUID(),
        eventType = "DEATH_NOTIFICATION",
        enrichmentFields = "a,b,c",
      ),
      ConsumerSubscription(
        consumerId = UUID.randomUUID(),
        eventType = "DEATH_NOTIFICATION",
        enrichmentFields = "a,b,c",
      ),
    )

    every { consumerSubscriptionRepository.findAll() }.returns(savedConsumerSubscriptions)

    val consumerSubscriptions = underTest.getConsumerSubscriptions().toList()

    assertThat(consumerSubscriptions).hasSize(3)
    assertThat(consumerSubscriptions).isEqualTo(savedConsumerSubscriptions.toList())
  }

  @Test
  fun `getSubscriptionsForConsumer gets all consumer subscriptions for id`() {
    val savedConsumerSubscriptions = listOf(
      ConsumerSubscription(
        consumerId = consumer.id,
        eventType = "DEATH_NOTIFICATION",
        enrichmentFields = "a,b,c",
      ),
      ConsumerSubscription(
        consumerId = consumer.id,
        eventType = "DEATH_NOTIFICATION",
        enrichmentFields = "a,b,c",
      ),
      ConsumerSubscription(
        consumerId = consumer.id,
        eventType = "DEATH_NOTIFICATION",
        enrichmentFields = "a,b,c",
      ),
    )

    every { consumerSubscriptionRepository.findAllByConsumerId(consumer.id) }.returns(savedConsumerSubscriptions)

    val consumerSubscriptions = underTest.getSubscriptionsForConsumer(consumer.id).toList()

    assertThat(consumerSubscriptions).hasSize(3)
    assertThat(consumerSubscriptions).isEqualTo(savedConsumerSubscriptions.toList())
  }

  @Test
  fun `addConsumerSubscription adds new subscription if consumer exists`() {
    every { consumerRepository.findByIdOrNull(consumer.id) }.returns(consumer)
    every { consumerSubscriptionRepository.save(any()) }.returns(consumerSubscription)

    underTest.addConsumerSubscription(consumer.id, consumerSubRequest)

    verify(exactly = 1) {
      consumerSubscriptionRepository.save(
        withArg {
          assertThat(it.consumerId).isEqualTo(consumer.id)
          assertThat(it.oauthClientId).isEqualTo(consumerSubRequest.oauthClientId)
          assertThat(it.enrichmentFields).isEqualTo(consumerSubRequest.enrichmentFields)
          assertThat(it.eventType).isEqualTo(consumerSubRequest.eventType)
        },
      )
    }
  }

  @Test
  fun `updateConsumerSubscription updates subscription`() {
    every { consumerRepository.findByIdOrNull(consumer.id) }.returns(consumer)
    every { consumerSubscriptionRepository.findByIdOrNull(consumerSubscription.id) }.returns(consumerSubscription)

    every { consumerSubscriptionRepository.save(any()) }.returns(consumerSubscription)

    underTest.updateConsumerSubscription(consumer.id, consumerSubscription.id, consumerSubRequest)

    verify(exactly = 1) {
      consumerSubscriptionRepository.save(
        withArg {
          assertThat(it.consumerId).isEqualTo(consumer.id)
          assertThat(it.oauthClientId).isEqualTo(consumerSubRequest.oauthClientId)
          assertThat(it.enrichmentFields).isEqualTo(consumerSubRequest.enrichmentFields)
          assertThat(it.eventType).isEqualTo(consumerSubRequest.eventType)
        },
      )
    }
  }

  @Test
  fun `updateConsumerSubscription does not update subscription if subscription does not exist`() {
    every { consumerSubscriptionRepository.findByIdOrNull(consumerSubscription.id) }.returns(null)

    val exception = assertThrows<ConsumerSubscriptionNotFoundException> {
      underTest.updateConsumerSubscription(consumer.id, consumerSubscription.id, consumerSubRequest)
    }

    assertThat(exception.message).isEqualTo("Subscription ${consumerSubscription.id} not found")

    verify(exactly = 0) { consumerSubscriptionRepository.save(any()) }
  }

  @Test
  fun `addConsumer adds consumer`() {
    val consumerRequest = ConsumerRequest(
      name = "Consumer",
    )

    every { consumerRepository.save(any()) }.returns(consumer)

    underTest.addConsumer(consumerRequest)

    verify(exactly = 1) {
      consumerRepository.save(
        withArg {
          assertThat(it.name).isEqualTo(consumerRequest.name)
        },
      )
    }
  }

  private val consumer = Consumer(name = "Base Consumer")
  private val consumerSubscription = ConsumerSubscription(
    consumerId = consumer.id,
    oauthClientId = "pollClientId",
    eventType = "DEATH_NOTIFICATION",
    enrichmentFields = "a,b,c",
  )
  private val consumerSubRequest = ConsumerSubRequest(
    eventType = "DEATH_NOTIFICATIONNew",
    enrichmentFields = "a,b,c,New",
    oauthClientId = "callbackClientIdNew",
  )
}
