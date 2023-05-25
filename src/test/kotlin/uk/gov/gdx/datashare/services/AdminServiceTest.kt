package uk.gov.gdx.datashare.services

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.enums.CognitoClientType
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.models.AcquirerSubscriptionDto
import uk.gov.gdx.datashare.models.CognitoClientResponse
import uk.gov.gdx.datashare.models.CreateAcquirerRequest
import uk.gov.gdx.datashare.repositories.Acquirer

class AdminServiceTest {
  private val cognitoService = mockk<CognitoService>()
  private val acquirerService = mockk<AcquirersService>()
  private val suppliersService = mockk<SuppliersService>()
  private val adminActionAlertsService = mockk<AdminActionAlertsService>()

  private val acquirer = Acquirer(name = "testacquirer")
  private val cognitoClientResponse = CognitoClientResponse("name", "id", "secret")

  private val underTest = AdminService(
    cognitoService,
    acquirerService,
    suppliersService,
    adminActionAlertsService,
  )

  @Test
  fun `createAcquirer creates an acquirer`() {
    justRun { adminActionAlertsService.noticeAction(any()) }
    every { acquirerService.addAcquirer(any()) } returns acquirer
    every { cognitoService.createUserPoolClient(any()) } returns cognitoClientResponse
    val acquirerSubscriptionDto = mockk<AcquirerSubscriptionDto>()
    every { acquirerService.addAcquirerSubscription(any(), any()) } returns acquirerSubscriptionDto

    val request = CreateAcquirerRequest(
      "name",
      EventType.TEST_EVENT,
      emptyList(),
    )
    val response = underTest.createAcquirer(request)

    assertThat(response.clientId).isEqualTo(cognitoClientResponse.clientId)
    assertThat(response.clientName).isEqualTo(cognitoClientResponse.clientName)
    assertThat(response.clientSecret).isEqualTo(cognitoClientResponse.clientSecret)
    assertThat(response.queueUrl).isNull()
    verify(exactly = 1) {
      acquirerService.addAcquirer(
        withArg {
          assertThat(it.name).isEqualTo("name")
        },
      )
    }
    verify(exactly = 1) {
      cognitoService.createUserPoolClient(
        withArg {
          assertThat(it.clientName).isEqualTo("name")
          assertThat(it.clientTypes).containsExactly(CognitoClientType.ACQUIRER)
        },
      )
    }
    verify(exactly = 1) {
      acquirerService.addAcquirerSubscription(
        withArg { assertThat(it).isEqualTo(acquirer.id) },
        withArg {
          assertThat(it.oauthClientId).isEqualTo("id")
          assertThat(it.eventType).isEqualTo(EventType.TEST_EVENT)
          assertThat(it.enrichmentFields).isEqualTo(emptyList<EnrichmentField>())
          assertThat(it.enrichmentFieldsIncludedInPoll).isFalse()
          assertThat(it.queueName).isNull()
          assertThat(it.principalArn).isNull()
        },
      )
    }
  }

  @Test
  fun `createAcquirer creates an acquirer with a queue`() {
    justRun { adminActionAlertsService.noticeAction(any()) }
    every { acquirerService.addAcquirer(any()) } returns acquirer
    val acquirerSubscriptionDto = mockk<AcquirerSubscriptionDto>()
    every { acquirerSubscriptionDto.queueUrl } returns "queueurl"
    every { acquirerService.addAcquirerSubscription(any(), any()) } returns acquirerSubscriptionDto

    val request = CreateAcquirerRequest(
      "name",
      EventType.TEST_EVENT,
      emptyList(),
      queueName = "acq_queue",
      principalArn = "principal",
    )
    val response = underTest.createAcquirer(request)

    assertThat(response.clientId).isNull()
    assertThat(response.clientName).isNull()
    assertThat(response.clientSecret).isNull()
    assertThat(response.queueUrl).isEqualTo("queueurl")
    verify(exactly = 1) {
      acquirerService.addAcquirer(
        withArg {
          assertThat(it.name).isEqualTo("name")
        },
      )
    }
    verify(exactly = 1) {
      acquirerService.addAcquirerSubscription(
        withArg { assertThat(it).isEqualTo(acquirer.id) },
        withArg {
          assertThat(it.oauthClientId).isNull()
          assertThat(it.eventType).isEqualTo(EventType.TEST_EVENT)
          assertThat(it.enrichmentFields).isEqualTo(emptyList<EnrichmentField>())
          assertThat(it.enrichmentFieldsIncludedInPoll).isFalse()
          assertThat(it.queueName).isEqualTo("acq_queue")
          assertThat(it.principalArn).isEqualTo("principal")
        },
      )
    }
  }
}
