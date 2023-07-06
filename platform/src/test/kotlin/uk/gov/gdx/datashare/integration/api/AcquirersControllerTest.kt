package uk.gov.gdx.datashare.integration.api

import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.integration.SqsIntegrationTestBase
import java.util.*

class AcquirersControllerTest : SqsIntegrationTestBase() {
  @Test
  fun `Returns 403 for non valid scope for get acquirers`() {
    webTestClient.get()
      .uri("/acquirers")
      .headers(setAuthorisation("len", listOf(""), listOf("events/consume")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `Returns 403 for non valid scope for add acquirer`() {
    webTestClient.post()
      .uri("/acquirers")
      .headers(setAuthorisation("len", listOf(""), listOf("events/consume")))
      .bodyValue(mapOf("name" to "DWP"))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `Returns 403 for non valid scope for get acquirer subscriptions`() {
    webTestClient.get()
      .uri("/acquirers/subscriptions")
      .headers(setAuthorisation("len", listOf(""), listOf("events/consume")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `Returns 403 for non valid scope for get acquirer's subscriptions`() {
    webTestClient.get()
      .uri("/acquirers/${UUID.randomUUID()}/subscriptions")
      .headers(setAuthorisation("len", listOf(""), listOf("events/consume")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `Returns 403 for non valid scope for add acquirer subscription`() {
    webTestClient.post()
      .uri("/acquirers/${UUID.randomUUID()}/subscriptions")
      .headers(setAuthorisation("len", listOf(""), listOf("events/consume")))
      .bodyValue(mapOf("eventType" to "DEATH_NOTIFICATION", "enrichmentFields" to listOf("firstNames"), "oauthClientId" to "abc"))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `Returns 403 for non valid scope for update acquirer subscription`() {
    webTestClient.put()
      .uri("/acquirers/${UUID.randomUUID()}/subscriptions/${UUID.randomUUID()}")
      .headers(setAuthorisation("len", listOf(""), listOf("events/consume")))
      .bodyValue(mapOf("eventType" to "DEATH_NOTIFICATION", "enrichmentFields" to listOf("firstNames"), "oauthClientId" to "abc"))
      .exchange()
      .expectStatus()
      .isForbidden
  }
}
