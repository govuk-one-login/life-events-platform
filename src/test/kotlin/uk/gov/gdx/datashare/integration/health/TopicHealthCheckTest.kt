package uk.gov.gdx.datashare.integration.health

import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.integration.SqsIntegrationTestBase

class TopicHealthCheckTest : SqsIntegrationTestBase() {

  @Test
  fun `Outbound topic health ok`() {
    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
      .jsonPath("components.event-health.status").isEqualTo("UP")
      .jsonPath("components.event-health.details.topicArn").isEqualTo(sqsPropertiesSpy.eventTopicConfig().arn)
      .jsonPath("components.event-health.details.subscriptionsConfirmed").isEqualTo(0)
      .jsonPath("components.event-health.details.subscriptionsPending").isEqualTo(0)
  }
}
