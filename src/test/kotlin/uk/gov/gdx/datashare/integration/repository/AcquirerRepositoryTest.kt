package uk.gov.gdx.datashare.integration.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.gdx.datashare.repositories.AcquirerRepository
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.helpers.builders.AcquirerBuilder
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.integration.MockIntegrationTestBase
import java.time.LocalDateTime

class AcquirerRepositoryTest(@Autowired private val acquirerRepository: AcquirerRepository) :
  MockIntegrationTestBase() {
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
}
