package uk.gov.gdx.datashare.uk.gov.gdx.datashare.integration.api

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.enums.CognitoClientType
import uk.gov.gdx.datashare.integration.SqsIntegrationTestBase
import uk.gov.gdx.datashare.models.CognitoClientResponse
import uk.gov.gdx.datashare.services.CognitoService
import uk.gov.gdx.datashare.services.OutboundEventQueueService
import uk.gov.gdx.datashare.services.ScheduledJobService

class AdminControllerTest : SqsIntegrationTestBase() {
  @MockkBean
  private lateinit var cognitoService: CognitoService

  @MockkBean
  private lateinit var outboundEventQueueService: OutboundEventQueueService

  @MockkBean
  private lateinit var scheduledJobService: ScheduledJobService

  @Test
  fun `Creates an acquirer`() {
    every { cognitoService.createUserPoolClient(any()) } returns CognitoClientResponse("testname", "id", "secret")

    webTestClient.post()
      .uri("/admin/acquirer")
      .headers(setAuthorisation("admin", listOf(""), listOf("events/admin")))
      .bodyValue(
        mapOf(
          "acquirerName" to "testname",
          "eventType" to "DEATH_NOTIFICATION",
          "enrichmentFields" to listOf("firstNames"),
          "enrichmentFieldsIncludedInPoll" to false,
        ),
      )
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("$.clientName").isEqualTo("testname")
      .jsonPath("$.clientId").isEqualTo("id")
      .jsonPath("$.clientSecret").isEqualTo("secret")
    verify(exactly = 1) {
      cognitoService.createUserPoolClient(
        withArg {
          assertThat(it.clientName).isEqualTo("testname")
          assertThat(it.clientTypes).containsExactly(CognitoClientType.ACQUIRER)
        },
      )
    }
  }

  @Test
  fun `Creates a queue acquirer`() {
    every { outboundEventQueueService.createAcquirerQueue(any(), any()) } returns "queueurl"

    webTestClient.post()
      .uri("/admin/acquirer")
      .headers(setAuthorisation("admin", listOf(""), listOf("events/admin")))
      .bodyValue(
        mapOf(
          "acquirerName" to "testname2",
          "eventType" to "DEATH_NOTIFICATION",
          "enrichmentFields" to listOf("firstNames"),
          "enrichmentFieldsIncludedInPoll" to false,
          "queueName" to "acq_test",
          "principalArn" to "principal",
        ),
      )
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("$.queueUrl").isEqualTo("queueurl")
    verify(exactly = 1) {
      outboundEventQueueService.createAcquirerQueue(
        withArg { assertThat(it).isEqualTo("acq_test") },
        withArg { assertThat(it).isEqualTo("principal") },
      )
    }
  }

  @Test
  fun `Returns 403 for non valid scope for create acquirer`() {
    webTestClient.post()
      .uri("/admin/acquirer")
      .headers(setAuthorisation("len", listOf(""), listOf("events/consume")))
      .bodyValue(
        mapOf(
          "acquirerName" to "testname",
          "eventType" to "DEATH_NOTIFICATION",
          "enrichmentFields" to listOf("firstNames"),
          "enrichmentFieldsIncludedInPoll" to false,
        ),
      )
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `Returns 403 for non valid scope for create supplier`() {
    webTestClient.post()
      .uri("/admin/supplier")
      .headers(setAuthorisation("len", listOf(""), listOf("events/consume")))
      .bodyValue(
        mapOf(
          "clientName" to "testname",
          "eventType" to "DEATH_NOTIFICATION",
        ),
      )
      .exchange()
      .expectStatus()
      .isForbidden
  }
}
