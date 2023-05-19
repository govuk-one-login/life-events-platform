package uk.gov.gdx.datashare.uk.gov.gdx.datashare.integration.respository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
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
}
