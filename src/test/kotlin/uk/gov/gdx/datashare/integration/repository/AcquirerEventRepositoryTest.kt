/*
package uk.gov.gdx.datashare.uk.gov.gdx.datashare.integration.repository

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.gdx.datashare.repositories.*
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.helpers.builders.AcquirerBuilder
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.helpers.builders.AcquirerEventBuilder
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.helpers.builders.AcquirerSubscriptionBuilder
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.helpers.compareIgnoringNanos
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.integration.MockIntegrationTestBase
import java.time.LocalDateTime

class AcquirerEventRepositoryTest(
  @Autowired private val acquirerRepository: AcquirerRepository,
  @Autowired private val acquirerSubscriptionRepository: AcquirerSubscriptionRepository,
  @Autowired private val acquirerEventRepository: AcquirerEventRepository,
  @Autowired private val supplierRepository: SupplierRepository,
  @Autowired private val supplierSubscriptionRepository: SupplierSubscriptionRepository,
  @Autowired private val supplierEventRepository: SupplierEventRepository,
) : MockIntegrationTestBase() {
  @Test
  fun `findByClientIdAndId returns the correct value`() {
    val acquirer = acquirerRepository.save(AcquirerBuilder().build())
    val checkedSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder().also {
        it.acquirerId = acquirer.id
        it.oauthClientId = "test"
      }.build(),
    )
    val notCheckedSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder().also {
        it.acquirerId = acquirer.id
        it.oauthClientId = null
      }.build(),
    )
    val deletedEvent = acquirerEventRepository.save(
      AcquirerEventBuilder(
        acquirerSubscriptionId = checkedSubscription.id,
        new = true,
        deletedAt = LocalDateTime.now(),
      ).build(),
    )
    val incorrectSubscriptionEvent = acquirerEventRepository.save(
      AcquirerEventBuilder(
        acquirerSubscriptionId = notCheckedSubscription.id,
        new = true,
      ).build(),
    )
    val returnedEvent = acquirerEventRepository.save(
      AcquirerEventBuilder(
        acquirerSubscriptionId = checkedSubscription.id,
        new = true,
      ).build(),
    )

    assertThat(
      acquirerEventRepository.findByClientIdAndId("test", ac),
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
*/
