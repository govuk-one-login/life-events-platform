package uk.gov.gdx.datashare.uk.gov.gdx.datashare.integration.api

import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.integration.SqsIntegrationTestBase
import java.util.*

class SuppliersControllerTest : SqsIntegrationTestBase() {
  @Test
  fun `Returns 403 for non valid scope for get suppliers`() {
    webTestClient.get()
      .uri("/suppliers")
      .headers(setAuthorisation("len", listOf(""), listOf("events/consume")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `Returns 403 for non valid scope for add supplier`() {
    webTestClient.post()
      .uri("/suppliers")
      .headers(setAuthorisation("len", listOf(""), listOf("events/consume")))
      .bodyValue(mapOf("name" to "DWP"))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `Returns 403 for non valid scope for get supplier subscriptions`() {
    webTestClient.get()
      .uri("/suppliers/subscriptions")
      .headers(setAuthorisation("len", listOf(""), listOf("events/consume")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `Returns 403 for non valid scope for get supplier's subscriptions`() {
    webTestClient.get()
      .uri("/suppliers/${UUID.randomUUID()}/subscriptions")
      .headers(setAuthorisation("len", listOf(""), listOf("events/consume")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `Returns 403 for non valid scope for add supplier subscriptions`() {
    webTestClient.post()
      .uri("/suppliers/${UUID.randomUUID()}/subscriptions")
      .headers(setAuthorisation("len", listOf(""), listOf("events/consume")))
      .bodyValue(mapOf("eventType" to "DEATH_NOTIFICATION", "clientId" to "test_id"))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `Returns 403 for non valid scope for update supplier subscriptions`() {
    webTestClient.put()
      .uri("/suppliers/${UUID.randomUUID()}/subscriptions/${UUID.randomUUID()}")
      .headers(setAuthorisation("len", listOf(""), listOf("events/consume")))
      .bodyValue(mapOf("eventType" to "DEATH_NOTIFICATION", "clientId" to "test_id"))
      .exchange()
      .expectStatus()
      .isForbidden
  }
}
