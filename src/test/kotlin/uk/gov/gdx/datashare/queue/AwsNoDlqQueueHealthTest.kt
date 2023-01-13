package uk.gov.gdx.datashare.queue

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest
import com.amazonaws.services.sqs.model.GetQueueAttributesResult
import com.amazonaws.services.sqs.model.GetQueueUrlResult
import com.amazonaws.services.sqs.model.QueueAttributeName
import com.amazonaws.services.sqs.model.QueueDoesNotExistException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.boot.actuate.health.Status

class AwsNoDlqQueueHealthTest {

  private val sqsClient = mock<AmazonSQS>()
  private val queueId = "some queue id"
  private val queueUrl = "some queue url"
  private val queueName = "some queue"
  private val messagesOnQueueCount = 123
  private val messagesInFlightCount = 456
  private val queueHealth = AwsQueueHealth(AwsQueue(queueId, sqsClient, queueName))

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
    whenever(sqsClient.getQueueUrl(anyString())).thenThrow(QueueDoesNotExistException::class.java)

    val health = queueHealth.health()

    assertThat(health.status).isEqualTo(Status.DOWN)
  }

  @Test
  fun `should show exception causing status DOWN`() {
    whenever(sqsClient.getQueueUrl(anyString())).thenThrow(QueueDoesNotExistException::class.java)

    val health = queueHealth.health()

    assertThat(health.details["error"] as String).contains("Exception")
  }

  @Test
  fun `should show queue name if status DOWN`() {
    whenever(sqsClient.getQueueUrl(anyString())).thenThrow(QueueDoesNotExistException::class.java)

    val health = queueHealth.health()

    assertThat(health.details["queueName"]).isEqualTo(queueName)
  }

  @Test
  fun `should show status DOWN if unable to retrieve queue attributes`() {
    whenever(sqsClient.getQueueUrl(anyString())).thenReturn(someGetQueueUrlResult())
    whenever(sqsClient.getQueueAttributes(someGetQueueAttributesRequest())).thenThrow(RuntimeException::class.java)

    val health = queueHealth.health()

    assertThat(health.status).isEqualTo(Status.DOWN)
  }

  @Test
  fun `should not show DLQ name`() {
    mockHealthyQueue()

    val health = queueHealth.health()

    assertThat(health.details["dlqName"]).isNull()
  }

  @Test
  fun `should not show interesting DLQ attributes`() {
    mockHealthyQueue()

    val health = queueHealth.health()

    assertThat(health.details["messagesOnDlq"]).isNull()
  }

  @Test
  fun `should not show DLQ name if no dlq exists`() {
    whenever(sqsClient.getQueueUrl(queueName)).thenReturn(someGetQueueUrlResult())
    whenever(sqsClient.getQueueAttributes(someGetQueueAttributesRequest())).thenReturn(
      someGetQueueAttributesResultWithoutDLQ(),
    )

    val health = queueHealth.health()

    assertThat(health.details["dlqName"]).isNull()
  }

  @Test
  fun `should not show DLQ status if no dlq exists`() {
    whenever(sqsClient.getQueueUrl(queueName)).thenReturn(someGetQueueUrlResult())
    whenever(sqsClient.getQueueAttributes(someGetQueueAttributesRequest())).thenReturn(
      someGetQueueAttributesResultWithoutDLQ(),
    )

    val health = queueHealth.health()

    assertThat(health.details["dlqStatus"]).isNull()
  }

  private fun mockHealthyQueue() {
    whenever(sqsClient.getQueueUrl(queueName)).thenReturn(someGetQueueUrlResult())
    whenever(sqsClient.getQueueAttributes(someGetQueueAttributesRequest())).thenReturn(
      someGetQueueAttributesResultWithoutDLQ(),
    )
  }

  private fun someGetQueueAttributesRequest() =
    GetQueueAttributesRequest(queueUrl).withAttributeNames(listOf(QueueAttributeName.All.toString()))

  private fun someGetQueueUrlResult(): GetQueueUrlResult = GetQueueUrlResult().withQueueUrl(queueUrl)
  private fun someGetQueueAttributesResultWithoutDLQ() = GetQueueAttributesResult().withAttributes(
    mapOf(
      "ApproximateNumberOfMessages" to "$messagesOnQueueCount",
      "ApproximateNumberOfMessagesNotVisible" to "$messagesInFlightCount",
    ),
  )
}
