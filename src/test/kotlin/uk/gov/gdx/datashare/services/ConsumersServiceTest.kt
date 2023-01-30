package uk.gov.gdx.datashare.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import uk.gov.gdx.datashare.config.ConsumerSubscriptionNotFoundException
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.models.ConsumerRequest
import uk.gov.gdx.datashare.models.ConsumerSubRequest
import uk.gov.gdx.datashare.repositories.*
import java.util.*

class ConsumersServiceTest {
  private val consumerSubscriptionRepository = mockk<ConsumerSubscriptionRepository>()
  private val consumerRepository = mockk<ConsumerRepository>()
  private val consumerSubscriptionEnrichmentFieldRepository = mockk<ConsumerSubscriptionEnrichmentFieldRepository>()

  private val underTest = ConsumersService(
    consumerSubscriptionRepository,
    consumerRepository,
    consumerSubscriptionEnrichmentFieldRepository,
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
        eventType = EventType.DEATH_NOTIFICATION,
      ),
      ConsumerSubscription(
        consumerId = UUID.randomUUID(),
        eventType = EventType.DEATH_NOTIFICATION,
      ),
      ConsumerSubscription(
        consumerId = UUID.randomUUID(),
        eventType = EventType.DEATH_NOTIFICATION,
      ),
    )

    val enrichmentFields = savedConsumerSubscriptions.map {
      ConsumerSubscriptionEnrichmentField(
        consumerSubscriptionId = it.consumerSubscriptionId,
        enrichmentField = "a",
      )
    }
    savedConsumerSubscriptions.forEach {
      every { consumerSubscriptionEnrichmentFieldRepository.findAllByConsumerSubscriptionId(it.id) }.returns(
        listOf(enrichmentFields.find { enrichmentField -> enrichmentField.consumerSubscriptionId == it.id }!!),
      )
    }

    every { consumerSubscriptionRepository.findAll() }.returns(savedConsumerSubscriptions)

    val consumerSubscriptions = underTest.getConsumerSubscriptions().toList()

    assertThat(consumerSubscriptions).hasSize(3)
    consumerSubscriptions.forEach {
      val savedConsumerSubscription =
        savedConsumerSubscriptions.find { savedConsumerSubscription -> savedConsumerSubscription.consumerSubscriptionId == it.consumerSubscriptionId }
      val savedEnrichmentFields =
        enrichmentFields.filter { enrichmentField -> enrichmentField.consumerSubscriptionId == it.consumerSubscriptionId }
      assertThat(it.consumerId).isEqualTo(savedConsumerSubscription?.consumerId)
      assertThat(it.eventType).isEqualTo(savedConsumerSubscription?.eventType)
      assertThat(it.enrichmentFields).isEqualTo(savedEnrichmentFields.map { enrichmentField -> enrichmentField.enrichmentField })
    }
  }

  @Test
  fun `getSubscriptionsForConsumer gets all consumer subscriptions for id`() {
    val savedConsumerSubscriptions = listOf(
      ConsumerSubscription(
        consumerId = consumer.id,
        eventType = EventType.DEATH_NOTIFICATION,
      ),
      ConsumerSubscription(
        consumerId = consumer.id,
        eventType = EventType.DEATH_NOTIFICATION,
      ),
      ConsumerSubscription(
        consumerId = consumer.id,
        eventType = EventType.DEATH_NOTIFICATION,
      ),
    )

    val enrichmentFields = savedConsumerSubscriptions.map {
      ConsumerSubscriptionEnrichmentField(
        consumerSubscriptionId = it.consumerSubscriptionId,
        enrichmentField = "a",
      )
    }

    every { consumerSubscriptionRepository.findAllByConsumerId(consumer.id) }.returns(savedConsumerSubscriptions)
    savedConsumerSubscriptions.forEach {
      every { consumerSubscriptionEnrichmentFieldRepository.findAllByConsumerSubscriptionId(it.id) }.returns(
        listOf(enrichmentFields.find { enrichmentField -> enrichmentField.consumerSubscriptionId == it.id }!!),
      )
    }

    val consumerSubscriptions = underTest.getSubscriptionsForConsumer(consumer.id).toList()

    assertThat(consumerSubscriptions).hasSize(3)
    consumerSubscriptions.forEach {
      val savedConsumerSubscription =
        savedConsumerSubscriptions.find { savedConsumerSubscription -> savedConsumerSubscription.consumerSubscriptionId == it.consumerSubscriptionId }
      val savedEnrichmentFields =
        enrichmentFields.filter { enrichmentField -> enrichmentField.consumerSubscriptionId == it.consumerSubscriptionId }
      assertThat(it.consumerId).isEqualTo(savedConsumerSubscription?.consumerId)
      assertThat(it.eventType).isEqualTo(savedConsumerSubscription?.eventType)
      assertThat(it.enrichmentFields).isEqualTo(savedEnrichmentFields.map { enrichmentField -> enrichmentField.enrichmentField })
    }
  }

  @Test
  fun `addConsumerSubscription adds new subscription if consumer exists`() {
    every { consumerRepository.findByIdOrNull(consumer.id) }.returns(consumer)
    every { consumerSubscriptionRepository.save(any()) }.returns(consumerSubscription)
    every {
      consumerSubscriptionEnrichmentFieldRepository.saveAll(any<Iterable<ConsumerSubscriptionEnrichmentField>>())
    }.returns(allEnrichmentFields)

    underTest.addConsumerSubscription(consumer.id, consumerSubRequest)
    verify(exactly = 1) {
      consumerSubscriptionRepository.save(
        withArg {
          assertThat(it.consumerId).isEqualTo(consumer.id)
          assertThat(it.oauthClientId).isEqualTo(consumerSubRequest.oauthClientId)
          assertThat(it.eventType).isEqualTo(consumerSubRequest.eventType)
        },
      )
    }
    verify(exactly = 1) {
      consumerSubscriptionEnrichmentFieldRepository.saveAll(
        withArg<Iterable<ConsumerSubscriptionEnrichmentField>> {
          assertThat(it).hasSize(2)
          assertThat(it.elementAt(0).consumerSubscriptionId).isEqualTo(it.elementAt(1).consumerSubscriptionId)
            .isEqualTo(consumerSubscription.id)
          assertThat(it.map { enrichmentField -> enrichmentField.enrichmentField }).isEqualTo(listOf("a", "New"))
        },
      )
    }
  }

  @Test
  fun `updateConsumerSubscription updates subscription`() {
    every { consumerRepository.findByIdOrNull(consumer.id) }.returns(consumer)
    every { consumerSubscriptionRepository.findByIdOrNull(consumerSubscription.id) }.returns(consumerSubscription)

    every { consumerSubscriptionRepository.save(any()) }.returns(consumerSubscription)
    every { consumerSubscriptionEnrichmentFieldRepository.deleteAllByConsumerSubscriptionId(consumerSubscription.id) }.returns(
      Unit,
    )
    every {
      consumerSubscriptionEnrichmentFieldRepository.saveAll(any<Iterable<ConsumerSubscriptionEnrichmentField>>())
    }.returns(allEnrichmentFields)

    underTest.updateConsumerSubscription(consumer.id, consumerSubscription.id, consumerSubRequest)

    verify(exactly = 1) {
      consumerSubscriptionRepository.save(
        withArg {
          assertThat(it.consumerId).isEqualTo(consumer.id)
          assertThat(it.oauthClientId).isEqualTo(consumerSubRequest.oauthClientId)
          assertThat(it.eventType).isEqualTo(consumerSubRequest.eventType)
        },
      )
    }
    verify(exactly = 1) {
      consumerSubscriptionEnrichmentFieldRepository.saveAll(
        withArg<Iterable<ConsumerSubscriptionEnrichmentField>> {
          assertThat(it).hasSize(2)
          assertThat(it.elementAt(0).consumerSubscriptionId).isEqualTo(it.elementAt(1).consumerSubscriptionId)
            .isEqualTo(consumerSubscription.id)
          assertThat(it.map { enrichmentField -> enrichmentField.enrichmentField }).isEqualTo(listOf("a", "New"))
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

  // add enrichment
  private val consumer = Consumer(name = "Base Consumer")
  private val consumerSubscription = ConsumerSubscription(
    consumerId = consumer.id,
    oauthClientId = "pollClientId",
    eventType = EventType.DEATH_NOTIFICATION,
  )
  private val consumerSubRequest = ConsumerSubRequest(
    eventType = EventType.LIFE_EVENT,
    enrichmentFields = listOf("a", "New"),
    oauthClientId = "callbackClientIdNew",
  )
  private val consumerSubscriptionEnrichmentField = ConsumerSubscriptionEnrichmentField(
    consumerSubscriptionId = consumerSubscription.id,
    enrichmentField = "a",
  )
  private val newConsumerSubscriptionEnrichmentField = ConsumerSubscriptionEnrichmentField(
    consumerSubscriptionId = consumerSubscription.id,
    enrichmentField = "new",
  )
  private val allEnrichmentFields = listOf(consumerSubscriptionEnrichmentField, newConsumerSubscriptionEnrichmentField)
}
