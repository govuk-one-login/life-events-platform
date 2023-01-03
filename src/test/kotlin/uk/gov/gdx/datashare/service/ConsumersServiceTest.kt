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
import uk.gov.gdx.datashare.repository.Consumer
import uk.gov.gdx.datashare.repository.ConsumerRepository
import uk.gov.gdx.datashare.repository.ConsumerSubscription
import uk.gov.gdx.datashare.repository.ConsumerSubscriptionRepository
import java.util.UUID

class ConsumersServiceTest {
  private val consumerSubscriptionRepository = mockk<ConsumerSubscriptionRepository>()
  private val consumerRepository = mockk<ConsumerRepository>()

  private val underTest = ConsumersService(
    consumerSubscriptionRepository,
    consumerRepository,
  )

  @Test
  fun `getConsumers gets all consumers`() {
    runBlocking {
      val savedConsumers = flowOf(
        Consumer(name = "Consumer1"),
        Consumer(name = "Consumer2"),
        Consumer(name = "Consumer3")
      )

      coEvery { consumerRepository.findAll() }.returns(savedConsumers)

      val consumers = underTest.getConsumers().toList()

      assertThat(consumers).hasSize(3)
      assertThat(consumers).isEqualTo(savedConsumers.toList())
    }
  }

  @Test
  fun `getConsumerSubscriptions gets all consumer subscriptions`() {
    runBlocking {
      val savedConsumerSubscriptions = flowOf(
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

      coEvery { consumerSubscriptionRepository.findAll() }.returns(savedConsumerSubscriptions)

      val consumerSubscriptions = underTest.getConsumerSubscriptions().toList()

      assertThat(consumerSubscriptions).hasSize(3)
      assertThat(consumerSubscriptions).isEqualTo(savedConsumerSubscriptions.toList())
    }
  }

  @Test
  fun `getSubscriptionsForConsumer gets all consumer subscriptions for id`() {
    runBlocking {
      val savedConsumerSubscriptions = flowOf(
        ConsumerSubscription(
          consumerId = consumer.id,
          ingressEventType = "DEATH_NOTIFICATION",
          enrichmentFields = "a,b,c"
        ),
        ConsumerSubscription(
          consumerId = consumer.id,
          ingressEventType = "DEATH_NOTIFICATION",
          enrichmentFields = "a,b,c"
        ),
        ConsumerSubscription(
          consumerId = consumer.id,
          ingressEventType = "DEATH_NOTIFICATION",
          enrichmentFields = "a,b,c"
        ),
      )

      coEvery { consumerSubscriptionRepository.findAllByConsumerId(consumer.id) }.returns(savedConsumerSubscriptions)

      val consumerSubscriptions = underTest.getSubscriptionsForConsumer(consumer.id).toList()

      assertThat(consumerSubscriptions).hasSize(3)
      assertThat(consumerSubscriptions).isEqualTo(savedConsumerSubscriptions.toList())
    }
  }

  @Test
  fun `addConsumerSubscription adds new subscription if consumer exists`() {
    runBlocking {
      coEvery { consumerRepository.findById(consumer.id) }.returns(consumer)
      coEvery { consumerSubscriptionRepository.save(any()) }.returns(consumerSubscription)

      underTest.addConsumerSubscription(consumer.id, consumerSubRequest)

      coVerify(exactly = 1) {
        consumerSubscriptionRepository.save(
          withArg {
            assertThat(it.consumerId).isEqualTo(consumer.id)
            assertThat(it.pollClientId).isEqualTo(consumerSubRequest.pollClientId)
            assertThat(it.callbackClientId).isEqualTo(consumerSubRequest.callbackClientId)
            assertThat(it.pushUri).isEqualTo(consumerSubRequest.pushUri)
            assertThat(it.ninoRequired).isEqualTo(consumerSubRequest.ninoRequired)
            assertThat(it.enrichmentFields).isEqualTo(consumerSubRequest.enrichmentFields)
            assertThat(it.ingressEventType).isEqualTo(consumerSubRequest.ingressEventType)
          }
        )
      }
    }
  }

  @Test
  fun `updateConsumerSubscription updates subscription`() {
    runBlocking {
      coEvery { consumerRepository.findById(consumer.id) }.returns(consumer)
      coEvery { consumerSubscriptionRepository.findById(consumerSubscription.id) }.returns(consumerSubscription)

      coEvery { consumerSubscriptionRepository.save(any()) }.returns(consumerSubscription)

      underTest.updateConsumerSubscription(consumer.id, consumerSubscription.id, consumerSubRequest)

      coVerify(exactly = 1) {
        consumerSubscriptionRepository.save(
          withArg {
            assertThat(it.consumerId).isEqualTo(consumer.id)
            assertThat(it.pollClientId).isEqualTo(consumerSubRequest.pollClientId)
            assertThat(it.callbackClientId).isEqualTo(consumerSubRequest.callbackClientId)
            assertThat(it.pushUri).isEqualTo(consumerSubRequest.pushUri)
            assertThat(it.ninoRequired).isEqualTo(consumerSubRequest.ninoRequired)
            assertThat(it.enrichmentFields).isEqualTo(consumerSubRequest.enrichmentFields)
            assertThat(it.ingressEventType).isEqualTo(consumerSubRequest.ingressEventType)
          }
        )
      }
    }
  }

  @Test
  fun `updateConsumerSubscription does not update subscription if subscription does not exist`() {
    runBlocking {
      coEvery { consumerSubscriptionRepository.findById(consumerSubscription.id) }.returns(null)

      val exception = assertThrows<RuntimeException> {
        underTest.updateConsumerSubscription(consumer.id, consumerSubscription.id, consumerSubRequest)
      }

      assertThat(exception.message).isEqualTo("Subscription ${consumerSubscription.id} not found")

      coVerify(exactly = 0) { consumerSubscriptionRepository.save(any()) }
    }
  }

  @Test
  fun `addConsumer adds consumer`() {
    runBlocking {
      val consumerRequest = ConsumerRequest(
        name = "Consumer"
      )

      coEvery { consumerRepository.save(any()) }.returns(consumer)

      underTest.addConsumer(consumerRequest)

      coVerify(exactly = 1) {
        consumerRepository.save(
          withArg {
            assertThat(it.name).isEqualTo(consumerRequest.name)
          }
        )
      }
    }
  }

  private val consumer = Consumer(name = "Base Consumer")
  private val consumerSubscription = ConsumerSubscription(
    consumerId = consumer.id,
    callbackClientId = "callbackClientId",
    pollClientId = "pollClientId",
    pushUri = "pushUri",
    ninoRequired = false,
    ingressEventType = "DEATH_NOTIFICATION",
    enrichmentFields = "a,b,c"
  )
  private val consumerSubRequest = ConsumerSubRequest(
    ingressEventType = "DEATH_NOTIFICATIONNew",
    enrichmentFields = "a,b,c,New",
    callbackClientId = "callbackClientIdNew",
    pollClientId = "pollClientIdNew",
    pushUri = "pushUriNew",
    ninoRequired = true
  )
}
