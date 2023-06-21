package uk.gov.gdx.datashare.integration.actuator

import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability
import uk.gov.gdx.datashare.integration.SqsIntegrationTestBase

@AutoConfigureObservability
class PrometheusTest : SqsIntegrationTestBase() {
  @Test
  fun `Unauthenticated page is inaccessible`() {
    webTestClient.get()
      .uri("/prometheus")
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `Authenticated page is accessible`() {
    webTestClient
      .get()
      .uri("/prometheus")
      .headers { it.setBasicAuth("prometheus", "prometheus") }
      .exchange()
      .expectStatus()
      .isOk
  }
}
