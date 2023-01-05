package uk.gov.gdx.datashare.integration.api

import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.integration.IntegrationTestBase
import uk.gov.gdx.datashare.integration.SqsIntegrationTestBase

class EventsQueryTest : SqsIntegrationTestBase() {
  @Test
  fun `Get events successfully queries db`() {
    webTestClient.get()
      .uri("/events")
      .headers(setAuthorisation("event-query-test", listOf(""), listOf("events/consume")))
      .exchange()
      .expectStatus()
      .isOk
  }
}
