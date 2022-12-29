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
import uk.gov.gdx.datashare.repository.*
import java.util.*

class ConsumersServiceTest {
  private val consumerSubscriptionRepository = mockk<ConsumerSubscriptionRepository>()
  private val consumerRepository = mockk<ConsumerRepository>()
  private val egressEventTypeRepository = mockk<EgressEventTypeRepository>()
  private val underTest = ConsumersService(
    consumerSubscriptionRepository,
    consumerRepository,
    egressEventTypeRepository
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
        ConsumerSubscription(consumerId = UUID.randomUUID(), eventTypeId = UUID.randomUUID()),
        ConsumerSubscription(consumerId = UUID.randomUUID(), eventTypeId = UUID.randomUUID()),
        ConsumerSubscription(consumerId = UUID.randomUUID(), eventTypeId = UUID.randomUUID())
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
        ConsumerSubscription(consumerId = consumer.id, eventTypeId = UUID.randomUUID()),
        ConsumerSubscription(consumerId = consumer.id, eventTypeId = UUID.randomUUID()),
        ConsumerSubscription(consumerId = consumer.id, eventTypeId = UUID.randomUUID())
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
      coEvery { egressEventTypeRepository.save(any()) }.returns(egressEventType)
      coEvery { consumerSubscriptionRepository.save(any()) }.returns(consumerSubscription)

      underTest.addConsumerSubscription(consumer.id, consumerSubRequest)

      coVerify(exactly = 1) {
        egressEventTypeRepository.save(withArg {
          assertThat(it.enrichmentFields).isEqualTo(consumerSubRequest.enrichmentFields)
          assertThat(it.ingressEventType).isEqualTo(consumerSubRequest.ingressEventType)
          assertThat(it.description).isEqualTo("${consumerSubRequest.ingressEventType} for ${consumer.name}")
        })
      }

      coVerify(exactly = 1) {
        consumerSubscriptionRepository.save(withArg {
          assertThat(it.consumerId).isEqualTo(consumer.id)
          assertThat(it.eventTypeId).isEqualTo(egressEventType.id)
          assertThat(it.pollClientId).isEqualTo(consumerSubRequest.pollClientId)
          assertThat(it.callbackClientId).isEqualTo(consumerSubRequest.callbackClientId)
          assertThat(it.pushUri).isEqualTo(consumerSubRequest.pushUri)
          assertThat(it.ninoRequired).isEqualTo(consumerSubRequest.ninoRequired)
        })
      }
    }
  }

  @Test
  fun `addConsumerSubscription does not add new subscription if consumer does not exist`() {
    runBlocking {
      coEvery { consumerRepository.findById(consumer.id) }.returns(null)

      val exception = assertThrows<RuntimeException> {
        underTest.addConsumerSubscription(consumer.id, consumerSubRequest)
      }

      assertThat(exception.message).isEqualTo("Consumer ${consumer.id} not found")

      coVerify(exactly = 0) { egressEventTypeRepository.save(any()) }
      coVerify(exactly = 0) { consumerSubscriptionRepository.save(any()) }
    }
  }

  @Test
  fun `updateConsumerSubscription updates subscription`() {
    runBlocking {
      coEvery { consumerRepository.findById(consumer.id) }.returns(consumer)
      coEvery {
        egressEventTypeRepository.findByConsumerSubscriptionId(consumerSubscription.id)
      }.returns(egressEventType)
      coEvery { consumerSubscriptionRepository.findById(consumerSubscription.id) }.returns(consumerSubscription)

      coEvery { egressEventTypeRepository.save(any()) }.returns(EgressEventType(
        ingressEventType = consumerSubRequest.ingressEventType,
        description = "${consumerSubRequest.ingressEventType} for ${consumer.name}",
        enrichmentFields = consumerSubRequest.enrichmentFields
      ))
      coEvery { consumerSubscriptionRepository.save(any()) }.returns(consumerSubscription)

      underTest.updateConsumerSubscription(consumer.id, consumerSubscription.id, consumerSubRequest)

      coVerify(exactly = 1) {
        egressEventTypeRepository.save(withArg {
          assertThat(it.enrichmentFields).isEqualTo(consumerSubRequest.enrichmentFields)
          assertThat(it.ingressEventType).isEqualTo(consumerSubRequest.ingressEventType)
          assertThat(it.description).isEqualTo("${consumerSubRequest.ingressEventType} for ${consumer.name}")
        })
      }
      coVerify(exactly = 1) {
        consumerSubscriptionRepository.save(withArg {
          assertThat(it.consumerId).isEqualTo(consumer.id)
          assertThat(it.eventTypeId).isNotEqualTo(egressEventType.id)
          assertThat(it.pollClientId).isEqualTo(consumerSubRequest.pollClientId)
          assertThat(it.callbackClientId).isEqualTo(consumerSubRequest.callbackClientId)
          assertThat(it.pushUri).isEqualTo(consumerSubRequest.pushUri)
          assertThat(it.ninoRequired).isEqualTo(consumerSubRequest.ninoRequired)
        })
      }
    }
  }

  @Test
  fun `updateConsumerSubscription does not update subscription if consumer does not exist`() {
    runBlocking {
      coEvery { consumerRepository.findById(consumer.id) }.returns(null)

      val exception = assertThrows<RuntimeException> {
        underTest.updateConsumerSubscription(consumer.id, consumerSubscription.id, consumerSubRequest)
      }

      assertThat(exception.message).isEqualTo("Consumer ${consumer.id} not found")

      coVerify(exactly = 0) { egressEventTypeRepository.save(any()) }
      coVerify(exactly = 0) { consumerSubscriptionRepository.save(any()) }
    }
  }

  @Test
  fun `updateConsumerSubscription does not update subscription if event type does not exist`() {
    runBlocking {
      coEvery { consumerRepository.findById(consumer.id) }.returns(consumer)
      coEvery {
        egressEventTypeRepository.findByConsumerSubscriptionId(consumerSubscription.id)
      }.returns(null)

      val exception = assertThrows<RuntimeException> {
        underTest.updateConsumerSubscription(consumer.id, consumerSubscription.id, consumerSubRequest)
      }

      assertThat(exception.message).isEqualTo("Egress event type not found for consumer subscription ${consumerSubscription.id}")

      coVerify(exactly = 0) { egressEventTypeRepository.save(any()) }
      coVerify(exactly = 0) { consumerSubscriptionRepository.save(any()) }
    }
  }

  @Test
  fun `updateConsumerSubscription does not update subscription if subscription does not exist`() {
    runBlocking {
      coEvery { consumerRepository.findById(consumer.id) }.returns(consumer)
      coEvery {
        egressEventTypeRepository.findByConsumerSubscriptionId(consumerSubscription.id)
      }.returns(egressEventType)
      coEvery { consumerSubscriptionRepository.findById(consumerSubscription.id) }.returns(null)

      val exception = assertThrows<RuntimeException> {
        underTest.updateConsumerSubscription(consumer.id, consumerSubscription.id, consumerSubRequest)
      }

      assertThat(exception.message).isEqualTo("Subscription ${consumerSubscription.id} not found")

      coVerify(exactly = 0) { egressEventTypeRepository.save(any()) }
      coVerify(exactly = 0) { consumerSubscriptionRepository.save(any()) }
    }
  }

  private val consumer = Consumer(name = "Base Consumer")
  private val consumerSubscription = ConsumerSubscription(
    consumerId = consumer.id,
    eventTypeId = UUID.randomUUID(),
    callbackClientId = "callbackClientId",
    pollClientId = "pollClientId",
    pushUri = "pushUri",
    ninoRequired = false
  )
  private val consumerSubRequest = ConsumerSubRequest(
    ingressEventType = "DEATH_NOTIFICATIONNew",
    enrichmentFields = "a,b,c,New",
    callbackClientId = "callbackClientIdNew",
    pollClientId = "pollClientIdNew",
    pushUri = "pushUriNew",
    ninoRequired = true
  )
  private val egressEventType = EgressEventType(
    ingressEventType = "DEATH_NOTIFICATION",
    description = "DEATH_NOTIFICATION for ${consumer.id}",
    enrichmentFields = "a,b,c"
  )
}