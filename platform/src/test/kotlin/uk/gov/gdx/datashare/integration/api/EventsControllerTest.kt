package uk.gov.gdx.datashare.integration.api

import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.integration.SqsIntegrationTestBase
import java.util.*

class EventsControllerTest : SqsIntegrationTestBase() {
  @Test
  fun `Returns 201 for valid supplier for publish event`() {
    webTestClient.post()
      .uri("/events")
      .headers(setAuthorisation("len", listOf(""), listOf("events/publish")))
      .bodyValue(mapOf("eventType" to "DEATH_NOTIFICATION", "id" to "123456789"))
      .exchange()
      .expectStatus()
      .isCreated
  }

  @Test
  fun `Returns 403 for non valid supplier for publish event`() {
    webTestClient.post()
      .uri("/events")
      .headers(setAuthorisation("bad-supplier", listOf(""), listOf("events/publish")))
      .bodyValue(mapOf("eventType" to "DEATH_NOTIFICATION", "id" to "123456789"))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `Returns 403 for non valid scope for publish event`() {
    webTestClient.post()
      .uri("/events")
      .headers(setAuthorisation("len", listOf(""), listOf("events/consume")))
      .bodyValue(mapOf("eventType" to "DEATH_NOTIFICATION", "id" to "123456789"))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `Returns 403 for non valid scope for delete event`() {
    webTestClient.delete()
      .uri("/events/" + UUID.randomUUID())
      .headers(setAuthorisation("dwp-event-receiver", listOf(""), listOf("events/publish")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `Returns 404 on event not found for delete event`() {
    webTestClient.delete()
      .uri("/events/" + UUID.randomUUID())
      .headers(setAuthorisation("dwp-event-receiver", listOf(""), listOf("events/consume")))
      .exchange()
      .expectStatus()
      .isNotFound
  }

  @Test
  fun `Returns 403 for non valid scope for get event`() {
    webTestClient.get()
      .uri("/events/" + UUID.randomUUID())
      .headers(setAuthorisation("dwp-event-receiver", listOf(""), listOf("events/publish")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `Returns 403 for non valid scope for get events`() {
    webTestClient.get()
      .uri("/events")
      .headers(setAuthorisation("dwp-event-receiver", listOf(""), listOf("events/publish")))
      .exchange()
      .expectStatus()
      .isForbidden
  }
}
