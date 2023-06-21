package uk.gov.gdx.datashare.integration.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.gdx.datashare.repositories.AcquirerRepository
import uk.gov.gdx.datashare.repositories.AcquirerSubscriptionRepository
import uk.gov.gdx.datashare.helpers.builders.AcquirerBuilder
import uk.gov.gdx.datashare.helpers.builders.AcquirerSubscriptionBuilder
import uk.gov.gdx.datashare.integration.MockIntegrationTestBase
import java.time.LocalDateTime

class AcquirerRepositoryTest(
  @Autowired private val acquirerRepository: AcquirerRepository,
  @Autowired private val acquirerSubscriptionRepository: AcquirerSubscriptionRepository,
) : MockIntegrationTestBase() {
  @Test
  fun `findAllByWhenDeletedIsNull returns the correct values`() {
    val returnedAcquirer = acquirerRepository.save(AcquirerBuilder().build())
    val notReturnedAcquirer = acquirerRepository.save(AcquirerBuilder(whenDeleted = LocalDateTime.now()).build())

    val acquirers = acquirerRepository.findAllByWhenDeletedIsNull()

    assertThat(
      acquirers.filter { a -> a.acquirerId == returnedAcquirer.id },
    )
      .hasSize(1)

    assertThat(
      acquirers.filter { a -> a.acquirerId == notReturnedAcquirer.id },
    )
      .isEmpty()
  }

  @Test
  fun `findNameForAcquirerSubscriptionId returns the correct values`() {
    val acquirer = acquirerRepository.save(AcquirerBuilder(name = "acquirerone").build())
    val otherAcquirer = acquirerRepository.save(AcquirerBuilder(name = "acquirertwo").build())
    val acquirerSubscription = acquirerSubscriptionRepository.save(AcquirerSubscriptionBuilder(acquirerId = acquirer.id).build())
    val otherAcquirerSubscription = acquirerSubscriptionRepository.save(AcquirerSubscriptionBuilder(acquirerId = otherAcquirer.id).build())

    assertThat(
      acquirerRepository.findNameForAcquirerSubscriptionId(acquirerSubscription.id),
    )
      .isEqualTo(acquirer.name)

    assertThat(
      acquirerRepository.findNameForAcquirerSubscriptionId(otherAcquirerSubscription.id),
    )
      .isEqualTo(otherAcquirer.name)
  }
}
