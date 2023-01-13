package uk.gov.gdx.datashare.queue

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.GetTopicAttributesResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.boot.actuate.health.Status

class AwsTopicHealthTest {

  private val topicId = "some-topic-id"
  private val topicArn = "some-topic-arn"
  private val snsClient = mock<AmazonSNS>()
  private val topicHealth = AwsTopicHealth(AwsTopic(topicId, topicArn, snsClient))

  @Test
  fun `should show status UP`() {
    mockHealthyTopic()

    val health = topicHealth.health()

    assertThat(health.status).isEqualTo(Status.UP)
  }

  @Test
  fun `should show topic arn`() {
    mockHealthyTopic()

    val health = topicHealth.health()

    assertThat(health.details["topicArn"]).isEqualTo("some-topic-arn")
  }

  @Test
  fun `should show interesting topic attributes`() {
    mockHealthyTopic()

    val health = topicHealth.health()

    assertThat(health.details["subscriptionsConfirmed"]).isEqualTo("1")
    assertThat(health.details["subscriptionsPending"]).isEqualTo("2")
  }

  @Test
  fun `should show status DOWN if cannot retrieve attributes`() {
    mockUnhealthyTopic()

    val health = topicHealth.health()

    assertThat(health.status).isEqualTo(Status.DOWN)
  }

  @Test
  fun `should show exception if cannot retrieve attributes`() {
    mockUnhealthyTopic()

    val health = topicHealth.health()

    assertThat(health.details["error"] as String).contains("Exception")
    assertThat(health.details["error"] as String).contains("some exception")
  }

  fun mockUnhealthyTopic() {
    whenever(snsClient.getTopicAttributes(anyString()))
      .thenThrow(RuntimeException("some exception"))
  }

  fun mockHealthyTopic() {
    whenever(snsClient.getTopicAttributes(anyString())).thenReturn(
      GetTopicAttributesResult()
        .withAttributes(mapOf("SubscriptionsConfirmed" to "1", "SubscriptionsPending" to "2")),
    )
  }
}
