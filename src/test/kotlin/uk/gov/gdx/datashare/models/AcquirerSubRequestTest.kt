package uk.gov.gdx.datashare.uk.gov.gdx.datashare.models

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.models.AcquirerSubRequest

class AcquirerSubRequestTest {
  private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

  @Test
  fun `null queue name is accepted`() {
    val underTest = createRequestWithQueueName(null)

    val violations = validator.validate(underTest)
    assertThat(violations).isEmpty()
  }

  @Test
  fun `valid queue name is accepted`() {
    val underTest = createRequestWithQueueName("acq_ABCDEFGHIJabcdefghij----------__________123456789012345678901234567890123456")

    val violations = validator.validate(underTest)
    assertThat(violations).isEmpty()
  }

  @Test
  fun `fifo queue name is accepted`() {
    val underTest = createRequestWithQueueName("acq_valid.fifo")

    val violations = validator.validate(underTest)
    assertThat(violations).isEmpty()
  }

  @Test
  fun `long queue name is rejected`() {
    val underTest = createRequestWithQueueName("acq_ABCDEFGHIJabcdefghij----------__________1234567890123456789012345678901234567890toolong")

    val violations = validator.validate(underTest)
    assertThat(violations).isNotEmpty()
  }

  @Test
  fun `invalid queue name is rejected`() {
    val underTest = createRequestWithQueueName("acq_invalid.fifoname")

    val violations = validator.validate(underTest)
    assertThat(violations).isNotEmpty()
  }

  @Test
  fun `queue name without acq_ prefix is rejected`() {
    val underTest = createRequestWithQueueName("no_prefix")

    val violations = validator.validate(underTest)
    assertThat(violations).isNotEmpty()
  }

  private fun createRequestWithQueueName(queueName: String?) = AcquirerSubRequest(
    EventType.TEST_EVENT,
    null,
    emptyList(),
    false,
    queueName,
  )
}
