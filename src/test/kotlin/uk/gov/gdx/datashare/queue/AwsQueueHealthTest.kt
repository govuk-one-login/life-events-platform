package uk.gov.gdx.datashare.queue

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.actuate.health.Status
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.*

class AwsQueueHealthTest {

  private val sqsClient = mockk<SqsClient>()
  private val sqsDlqClient = mockk<SqsClient>()
  private val queueId = "some queue id"
  private val queueUrl = "some queue url"
  private val dlqUrl = "some dlq url"
  private val queueName = "some queue"
  private val dlqName = "some dlq"
  private val messagesOnQueueCount = 123
  private val messagesInFlightCount = 456
  private val messagesOnDLQCount = 789
  private val queueHealth = AwsQueueHealth(AwsQueue(queueId, sqsClient, queueName, sqsDlqClient, dlqName))

  @Test
  fun `should show status UP`() {
    mockHealthyQueue()

    val health = queueHealth.health()

    assertThat(health.status).isEqualTo(Status.UP)
  }

  @Test
  fun `should include queue name`() {
    mockHealthyQueue()

    val health = queueHealth.health()

    assertThat(health.details["queueName"]).isEqualTo(queueName)
  }

  @Test
  fun `should include interesting attributes`() {
    mockHealthyQueue()

    val health = queueHealth.health()

    assertThat(health.details["messagesOnQueue"]).isEqualTo("$messagesOnQueueCount")
    assertThat(health.details["messagesInFlight"]).isEqualTo("$messagesInFlightCount")
  }

  @Test
  fun `should show status DOWN`() {
    every { sqsClient.getQueueUrl(any<GetQueueUrlRequest>()) }.throws(QueueDoesNotExistException.builder().build())

    val health = queueHealth.health()

    assertThat(health.status).isEqualTo(Status.DOWN)
  }

  @Test
  fun `should show exception causing status DOWN`() {
    every { sqsClient.getQueueUrl(any<GetQueueUrlRequest>()) }.throws(QueueDoesNotExistException.builder().build())

    val health = queueHealth.health()

    assertThat(health.details["error"] as String).contains("Exception")
  }

  @Test
  fun `should show queue name if status DOWN`() {
    every { sqsClient.getQueueUrl(any<GetQueueUrlRequest>()) }.throws(QueueDoesNotExistException.builder().build())

    val health = queueHealth.health()

    assertThat(health.details["queueName"]).isEqualTo(queueName)
  }

  @Test
  fun `should show status DOWN if unable to retrieve queue attributes`() {
    every { sqsClient.getQueueUrl(any<GetQueueUrlRequest>()) }.returns(someGetQueueUrlResult())
    every { sqsClient.getQueueAttributes(someGetQueueAttributesRequest()) }.throws(RuntimeException())

    val health = queueHealth.health()

    assertThat(health.status).isEqualTo(Status.DOWN)
  }

  @Test
  fun `should show DLQ status UP`() {
    mockHealthyQueue()

    val health = queueHealth.health()

    assertThat(health.details["dlqStatus"]).isEqualTo("UP")
  }

  @Test
  fun `should show DLQ name`() {
    mockHealthyQueue()

    val health = queueHealth.health()

    assertThat(health.details["dlqName"]).isEqualTo(dlqName)
  }

  @Test
  fun `should show interesting DLQ attributes`() {
    mockHealthyQueue()

    val health = queueHealth.health()

    assertThat(health.details["messagesOnDlq"]).isEqualTo("$messagesOnDLQCount")
  }

  @Test
  fun `should show status DOWN if DLQ status is down`() {
    every { sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build()) }.returns(
      someGetQueueUrlResult(),
    )
    every { sqsClient.getQueueAttributes(someGetQueueAttributesRequest()) }.returns(
      someGetQueueAttributesResultWithoutDLQ(),
    )

    val health = queueHealth.health()

    assertThat(health.status).isEqualTo(Status.DOWN)
    assertThat(health.details["dlqStatus"]).isEqualTo("DOWN")
  }

  @Test
  fun `should show DLQ name if DLQ status is down`() {
    every { sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build()) }.returns(
      someGetQueueUrlResult(),
    )
    every { sqsClient.getQueueAttributes(someGetQueueAttributesRequest()) }.returns(
      someGetQueueAttributesResultWithoutDLQ(),
    )

    val health = queueHealth.health()

    assertThat(health.details["dlqName"]).isEqualTo(dlqName)
  }

  @Test
  fun `should show DLQ status DOWN if no RedrivePolicy attribute on main queue`() {
    every { sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build()) }.returns(
      someGetQueueUrlResult(),
    )
    every { sqsClient.getQueueAttributes(someGetQueueAttributesRequest()) }.returns(
      someGetQueueAttributesResultWithoutDLQ(),
    )

    val health = queueHealth.health()

    assertThat(health.details["dlqStatus"]).isEqualTo("DOWN")
  }

  @Test
  fun `should show DLQ status DOWN if DLQ not found`() {
    every { sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build()) }.returns(
      someGetQueueUrlResult(),
    )
    every { sqsClient.getQueueAttributes(someGetQueueAttributesRequest()) }.returns(
      someGetQueueAttributesResultWithDLQ(),
    )
    every { sqsDlqClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(dlqName).build()) }.throws(
      QueueDoesNotExistException.builder().build(),
    )

    val health = queueHealth.health()

    assertThat(health.details["dlqStatus"]).isEqualTo("DOWN")
  }

  @Test
  fun `should show exception causing DLQ status DOWN`() {
    every { sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build()) }.returns(
      someGetQueueUrlResult(),
    )
    every { sqsClient.getQueueAttributes(someGetQueueAttributesRequest()) }.returns(
      someGetQueueAttributesResultWithDLQ(),
    )
    every { sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build()) }.throws(
      QueueDoesNotExistException.builder().build(),
    )

    val health = queueHealth.health()

    assertThat(health.details["error"] as String).contains("Exception")
  }

  @Test
  fun `should show DLQ status DOWN if unable to retrieve DLQ attributes`() {
    every { sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build()) }.returns(
      someGetQueueUrlResult(),
    )
    every { sqsClient.getQueueAttributes(someGetQueueAttributesRequest()) }.returns(
      someGetQueueAttributesResultWithDLQ(),
    )
    every { sqsDlqClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(dlqName).build()) }.returns(
      someGetQueueUrlResultForDLQ(),
    )
    every { sqsDlqClient.getQueueAttributes(someGetQueueAttributesRequestForDLQ()) }.throws(RuntimeException())

    val health = queueHealth.health()

    assertThat(health.details["dlqStatus"]).isEqualTo("DOWN")
  }

  private fun mockHealthyQueue() {
    every { sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build()) }.returns(
      someGetQueueUrlResult(),
    )
    every { sqsClient.getQueueAttributes(someGetQueueAttributesRequest()) }.returns(
      someGetQueueAttributesResultWithDLQ(),
    )
    every { sqsDlqClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(dlqName).build()) }.returns(
      someGetQueueUrlResultForDLQ(),
    )
    every { sqsDlqClient.getQueueAttributes(someGetQueueAttributesRequestForDLQ()) }.returns(
      someGetQueueAttributesResultForDLQ(),
    )
  }

  private fun someGetQueueAttributesRequest() =
    GetQueueAttributesRequest.builder().queueUrl(queueUrl).attributeNames(QueueAttributeName.ALL).build()

  private fun someGetQueueUrlResult(): GetQueueUrlResponse = GetQueueUrlResponse.builder().queueUrl(queueUrl).build()
  private fun someGetQueueAttributesResultWithoutDLQ() = GetQueueAttributesResponse.builder().attributes(
    mapOf(
      QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES
        to "$messagesOnQueueCount",
      QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE to "$messagesInFlightCount",
    ),
  ).build()

  private fun someGetQueueAttributesResultWithDLQ() = GetQueueAttributesResponse.builder().attributes(
    mapOf(
      QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES to "$messagesOnQueueCount",
      QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE to "$messagesInFlightCount",
      QueueAttributeName.REDRIVE_POLICY to "any redrive policy",
    ),
  ).build()

  private fun someGetQueueAttributesRequestForDLQ() =
    GetQueueAttributesRequest.builder().queueUrl(dlqUrl).attributeNames(QueueAttributeName.ALL).build()

  private fun someGetQueueUrlResultForDLQ(): GetQueueUrlResponse =
    GetQueueUrlResponse.builder().queueUrl(dlqUrl).build()

  private fun someGetQueueAttributesResultForDLQ() = GetQueueAttributesResponse.builder().attributes(
    mapOf(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES to messagesOnDLQCount.toString()),
  ).build()
}
