package uk.gov.gdx.datashare.integration.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.gdx.datashare.helpers.builders.AcquirerBuilder
import uk.gov.gdx.datashare.helpers.builders.AcquirerSubscriptionBuilder
import uk.gov.gdx.datashare.helpers.builders.AcquirerSubscriptionEnrichmentFieldBuilder
import uk.gov.gdx.datashare.integration.MockIntegrationTestBase
import uk.gov.gdx.datashare.repositories.*

class AcquirerSubscriptionEnrichmentFieldRepositoryTest(
  @Autowired private val acquirerRepository: AcquirerRepository,
  @Autowired private val acquirerSubscriptionRepository: AcquirerSubscriptionRepository,
  @Autowired private val acquirerSubscriptionEnrichmentFieldRepository: AcquirerSubscriptionEnrichmentFieldRepository,
) : MockIntegrationTestBase() {
  @Test
  fun `findAllByAcquirerSubscriptionId returns the correct values`() {
    val acquirer = acquirerRepository.save(AcquirerBuilder().build())
    val checkedSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder(acquirerId = acquirer.id).build(),
    )
    val notCheckedSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder(acquirerId = acquirer.id).build(),
    )

    val returnedEnrichmentField = acquirerSubscriptionEnrichmentFieldRepository.save(
      AcquirerSubscriptionEnrichmentFieldBuilder(acquirerSubscriptionId = checkedSubscription.id).build(),
    )

    val notReturnedEnrichmentField = acquirerSubscriptionEnrichmentFieldRepository.save(
      AcquirerSubscriptionEnrichmentFieldBuilder(acquirerSubscriptionId = notCheckedSubscription.id).build(),
    )

    val enrichmentFields =
      acquirerSubscriptionEnrichmentFieldRepository.findAllByAcquirerSubscriptionId(checkedSubscription.id)

    assertThat(
      enrichmentFields.filter { e -> e.acquirerSubscriptionEnrichmentFieldId == returnedEnrichmentField.id },
    )
      .hasSize(1)

    assertThat(
      enrichmentFields.filter { e -> e.acquirerSubscriptionEnrichmentFieldId == notReturnedEnrichmentField.id },
    )
      .isEmpty()
  }

  @Test
  fun `deleteAllByAcquirerSubscriptionId deletes the correct values`() {
    val acquirer = acquirerRepository.save(AcquirerBuilder().build())
    val checkedSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder(acquirerId = acquirer.id).build(),
    )
    val notCheckedSubscription = acquirerSubscriptionRepository.save(
      AcquirerSubscriptionBuilder(acquirerId = acquirer.id).build(),
    )

    val enrichmentFieldToDelete = acquirerSubscriptionEnrichmentFieldRepository.save(
      AcquirerSubscriptionEnrichmentFieldBuilder(acquirerSubscriptionId = checkedSubscription.id).build(),
    )

    val enrichmentFieldToNotDelete = acquirerSubscriptionEnrichmentFieldRepository.save(
      AcquirerSubscriptionEnrichmentFieldBuilder(acquirerSubscriptionId = notCheckedSubscription.id).build(),
    )

    acquirerSubscriptionEnrichmentFieldRepository.deleteAllByAcquirerSubscriptionId(checkedSubscription.id)

    val deletedEnrichmentFields =
      acquirerSubscriptionEnrichmentFieldRepository.findAllByAcquirerSubscriptionId(checkedSubscription.id)

    val notDeletedEnrichmentFields =
      acquirerSubscriptionEnrichmentFieldRepository.findAllByAcquirerSubscriptionId(notCheckedSubscription.id)

    assertThat(
      deletedEnrichmentFields.filter { e -> e.acquirerSubscriptionEnrichmentFieldId == enrichmentFieldToDelete.id },
    )
      .isEmpty()

    assertThat(
      notDeletedEnrichmentFields.filter { e -> e.acquirerSubscriptionEnrichmentFieldId == enrichmentFieldToNotDelete.id },
    )
      .hasSize(1)
  }
}
