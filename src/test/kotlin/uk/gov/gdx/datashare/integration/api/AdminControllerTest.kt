package uk.gov.gdx.datashare.uk.gov.gdx.datashare.integration.api

import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.integration.SqsIntegrationTestBase

class AdminControllerTest : SqsIntegrationTestBase() {
  @Test
  fun `Returns 403 for non valid scope for create acquirer`() {
    webTestClient.post()
      .uri("/admin/acquirer")
      .headers(setAuthorisation("len", listOf(""), listOf("events/consume")))
      .bodyValue(
        mapOf(
          "clientName" to "testname",
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
