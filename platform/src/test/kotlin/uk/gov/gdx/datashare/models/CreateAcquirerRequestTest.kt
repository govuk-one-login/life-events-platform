package uk.gov.gdx.datashare.uk.gov.gdx.datashare.models

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.models.CreateAcquirerRequest

class CreateAcquirerRequestTest {
  private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

  @Test
  fun `null queue name is accepted`() {
    val underTest = CreateAcquirerRequest(
      acquirerName = "bob",
      EventType.TEST_EVENT,
      enrichmentFields = emptyList(),
      enrichmentFieldsIncludedInPoll = false,
      queueName = null,
      principalArn = null,
    )

    val violations = validator.validate(underTest)
    assertThat(violations).isEmpty()
  }

  @Test
  fun `valid queue name is accepted`() {
    val underTest =
      createRequestWithQueueName("acq_ABCDEFGHIJabcdefghij----------__________123456789012345678901234567890123456")

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
    val underTest =
      createRequestWithQueueName("acq_ABCDEFGHIJabcdefghij----------__________1234567890123456789012345678901234567890toolong")

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
  @Test
  fun `valid enrichment fields for given event type are accepted`() {
    val underTest = CreateAcquirerRequest(
      acquirerName = "bob",
      EventType.TEST_EVENT,
      enrichmentFields = listOf(EnrichmentField.SOURCE_ID, EnrichmentField.EVENT_TIME),
    )

    val violations = validator.validate(underTest)
    assertThat(violations).isEmpty()
  }

  @Test
  fun `invalid enrichment fields for given event type are rejected`() {
    val underTest = CreateAcquirerRequest(
      acquirerName = "bob",
      EventType.TEST_EVENT,
      enrichmentFields = listOf(EnrichmentField.SOURCE_ID, EnrichmentField.FORENAMES, EnrichmentField.SURNAME),
    )

    val violations = validator.validate(underTest)
    assertThat(violations).isNotEmpty()
  }

  private fun createRequestWithQueueName(queueName: String?) = CreateAcquirerRequest(
    acquirerName = "bob",
    EventType.TEST_EVENT,
    emptyList(),
    false,
    queueName,
    "principalArn",
  )
}
