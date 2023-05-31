package uk.gov.gdx.datashare.uk.gov.gdx.datashare.integration.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.repositories.AcquirerRepository
import uk.gov.gdx.datashare.repositories.AcquirerSubscriptionRepository
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.helpers.builders.AcquirerBuilder
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.helpers.builders.AcquirerSubscriptionBuilder
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.helpers.compareIgnoringNanos
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.integration.MockIntegrationTestBase
import java.time.LocalDateTime

class AcquirerSubscriptionRepositoryTest(
  @Autowired private val acquirerRepository: AcquirerRepository,
  @Autowired private val acquirerSubscriptionRepository: AcquirerSubscriptionRepository,
) : MockIntegrationTestBase() {

  @Test
  fun `findByAcquirerSubscriptionIdAndQueueNameIsNotNull returns the correct values`() {
    val acquirer = acquirerRepository.save(AcquirerBuilder().build())
    val returnedSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder().also {
        it.acquirerId = acquirer.id
        it.queueName = "test"
      }.build(),
    )
    val notReturnedSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder().also {
        it.acquirerId = acquirer.id
        it.queueName = null
      }.build(),
    )

    assertThat(
      acquirerSubscriptionRepository.findByAcquirerSubscriptionIdAndQueueNameIsNotNull(returnedSubscription.acquirerSubscriptionId),
    )
      .usingRecursiveComparison()
      .ignoringFields("new")
      .withComparatorForType(compareIgnoringNanos, LocalDateTime::class.java)
      .isEqualTo(returnedSubscription)

    assertThat(
      acquirerSubscriptionRepository.findByAcquirerSubscriptionIdAndQueueNameIsNotNull(notReturnedSubscription.acquirerSubscriptionId),
    ).isNull()
  }

  @Test
  fun `findAllByEventTypeAndWhenDeletedIsNull returns the correct values`() {
    val acquirer = acquirerRepository.save(AcquirerBuilder().build())
    val returnedSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder().also {
        it.acquirerId = acquirer.id
        it.queueName = "test"
        it.eventType = EventType.DEATH_NOTIFICATION
      }.build(),
    )
    val notReturnedSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder().also {
        it.acquirerId = acquirer.id
        it.queueName = null
        it.eventType = EventType.DEATH_NOTIFICATION
        it.whenDeleted = LocalDateTime.now()
      }.build(),
    )

    val subscriptions = acquirerSubscriptionRepository.findAllByEventTypeAndWhenDeletedIsNull(EventType.DEATH_NOTIFICATION)

    assertThat(
      subscriptions.filter { s -> s.acquirerSubscriptionId == returnedSubscription.id }
    )
      .hasSize(1)

    assertThat(
      subscriptions.filter { s -> s.acquirerSubscriptionId == notReturnedSubscription.id }
    )
      .isEmpty()
  }
}
