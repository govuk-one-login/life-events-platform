package uk.gov.gdx.datashare.models

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType

class AcquirerSubRequestTest {
  private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

  @Test
  fun `null queue name is accepted`() {
    val underTest = AcquirerSubRequest(
      EventType.TEST_EVENT,
      enrichmentFields = emptyList(),
      oauthClientId = "oauthid",
      queueName = null,
    )

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

  @ParameterizedTest
  @CsvSource(
    nullValues = ["NULL"],
    textBlock = """
oauthClient,NULL    ,NULL     ,NULL
NULL       ,acq_test,principal,NULL
NULL       ,NULL    ,NULL     ,Exactly one of oauthClientId and queueUrl must be specified
oauthClient,acq_test,principal,Exactly one of oauthClientId and queueUrl must be specified
NULL       ,acq_test,NULL     ,Both queueName and principalArn must be specified or both must be null
oauthClient,NULL    ,principal,Both queueName and principalArn must be specified or both must be null""",
  )
  fun `Only a single output method may be specified`(
    oauthClientId: String?,
    queueName: String?,
    principalArn: String?,
    errorMessage: String?,
  ) {
    val underTest = AcquirerSubRequest(
      EventType.TEST_EVENT,
      enrichmentFields = emptyList(),
      oauthClientId = oauthClientId,
      queueName = queueName,
      principalArn = principalArn,
    )

    val violations = validator.validate(underTest)
    if (errorMessage == null) {
      assertThat(violations).isEmpty()
    } else {
      assertThat(violations).isNotEmpty()
      assertThat(violations.first().message).isEqualTo(errorMessage)
    }
  }

  @Test
  fun `valid enrichment fields for given event type are accepted`() {
    val underTest = AcquirerSubRequest(
      EventType.TEST_EVENT,
      enrichmentFields = listOf(EnrichmentField.SOURCE_ID, EnrichmentField.EVENT_TIME),
      oauthClientId = "oauthid",
    )

    val violations = validator.validate(underTest)
    assertThat(violations).isEmpty()
  }

  @Test
  fun `invalid enrichment fields for given event type are rejected`() {
    val underTest = AcquirerSubRequest(
      EventType.TEST_EVENT,
      enrichmentFields = listOf(EnrichmentField.SOURCE_ID, EnrichmentField.FORENAMES, EnrichmentField.SURNAME),
      oauthClientId = "oauthid",
    )

    val violations = validator.validate(underTest)
    assertThat(violations).isNotEmpty()
  }

  private fun createRequestWithQueueName(queueName: String?) = AcquirerSubRequest(
    EventType.TEST_EVENT,
    null,
    emptyList(),
    false,
    queueName,
    "principalArn",
  )
}
