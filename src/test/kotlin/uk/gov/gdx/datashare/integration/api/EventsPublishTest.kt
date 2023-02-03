package uk.gov.gdx.datashare.integration.api

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParameterRequest
import software.amazon.awssdk.services.ssm.model.GetParameterResponse
import software.amazon.awssdk.services.ssm.model.Parameter
import uk.gov.gdx.datashare.integration.SqsIntegrationTestBase
import java.util.*

class EventsPublishTest : SqsIntegrationTestBase() {
  @Test
  fun `Events successfully publishes event`() {
    webTestClient.post()
      .uri("/events")
      .headers(setAuthorisation("len", listOf(""), listOf("events/publish")))
      .bodyValue(mapOf("eventType" to "DEATH_NOTIFICATION", "id" to "123456789"))
      .exchange()
      .expectStatus()
      .isOk
  }

  @Test
  fun `403 returns on non valid supplier`() {
    webTestClient.post()
      .uri("/events")
      .headers(setAuthorisation("bad-supplier", listOf(""), listOf("events/publish")))
      .bodyValue(mapOf("eventType" to "DEATH_NOTIFICATION", "id" to "123456789"))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `404 returns on event not found`() {
    webTestClient.get()
      .uri("/events/" + UUID.randomUUID())
      .headers(setAuthorisation("dwp-event-receiver", listOf(""), listOf("events/consume")))
      .exchange()
      .expectStatus()
      .isNotFound
  }

  @Test
  fun `404 returns on event not found for deletion`() {
    webTestClient.delete()
      .uri("/events/" + UUID.randomUUID())
      .headers(setAuthorisation("dwp-event-receiver", listOf(""), listOf("events/consume")))
      .exchange()
      .expectStatus()
      .isNotFound
  }
}
