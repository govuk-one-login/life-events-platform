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

class AwsQueueHealthTest {

  private val sqsClient = mock<AmazonSQS>()
  private val sqsDlqClient = mock<AmazonSQS>()
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
    whenever(sqsClient.getQueueUrl(queueName)).thenReturn(someGetQueueUrlResult())
    whenever(sqsClient.getQueueAttributes(someGetQueueAttributesRequest())).thenReturn(
      someGetQueueAttributesResultWithoutDLQ(),
    )

    val health = queueHealth.health()

    assertThat(health.status).isEqualTo(Status.DOWN)
    assertThat(health.details["dlqStatus"]).isEqualTo("DOWN")
  }

  @Test
  fun `should show DLQ name if DLQ status is down`() {
    whenever(sqsClient.getQueueUrl(queueName)).thenReturn(someGetQueueUrlResult())
    whenever(sqsClient.getQueueAttributes(someGetQueueAttributesRequest())).thenReturn(
      someGetQueueAttributesResultWithoutDLQ(),
    )

    val health = queueHealth.health()

    assertThat(health.details["dlqName"]).isEqualTo(dlqName)
  }

  @Test
  fun `should show DLQ status DOWN if no RedrivePolicy attribute on main queue`() {
    whenever(sqsClient.getQueueUrl(queueName)).thenReturn(someGetQueueUrlResult())
    whenever(sqsClient.getQueueAttributes(someGetQueueAttributesRequest())).thenReturn(
      someGetQueueAttributesResultWithoutDLQ(),
    )

    val health = queueHealth.health()

    assertThat(health.details["dlqStatus"]).isEqualTo("DOWN")
  }

  @Test
  fun `should show DLQ status DOWN if DLQ not found`() {
    whenever(sqsClient.getQueueUrl(queueName)).thenReturn(someGetQueueUrlResult())
    whenever(sqsClient.getQueueAttributes(someGetQueueAttributesRequest())).thenReturn(
      someGetQueueAttributesResultWithDLQ(),
    )
    whenever(sqsDlqClient.getQueueUrl(dlqName)).thenThrow(QueueDoesNotExistException::class.java)

    val health = queueHealth.health()

    assertThat(health.details["dlqStatus"]).isEqualTo("DOWN")
  }

  @Test
  fun `should show exception causing DLQ status DOWN`() {
    whenever(sqsClient.getQueueUrl(queueName)).thenReturn(someGetQueueUrlResult())
    whenever(sqsClient.getQueueAttributes(someGetQueueAttributesRequest())).thenReturn(
      someGetQueueAttributesResultWithDLQ(),
    )
    whenever(sqsDlqClient.getQueueUrl(dlqName)).thenThrow(QueueDoesNotExistException::class.java)

    val health = queueHealth.health()

    assertThat(health.details["error"] as String).contains("Exception")
  }

  @Test
  fun `should show DLQ status DOWN if unable to retrieve DLQ attributes`() {
    whenever(sqsClient.getQueueUrl(queueName)).thenReturn(someGetQueueUrlResult())
    whenever(sqsClient.getQueueAttributes(someGetQueueAttributesRequest())).thenReturn(
      someGetQueueAttributesResultWithDLQ(),
    )
    whenever(sqsDlqClient.getQueueUrl(dlqName)).thenReturn(someGetQueueUrlResultForDLQ())
    whenever(sqsDlqClient.getQueueAttributes(someGetQueueAttributesRequestForDLQ())).thenThrow(RuntimeException::class.java)

    val health = queueHealth.health()

    assertThat(health.details["dlqStatus"]).isEqualTo("DOWN")
  }

  private fun mockHealthyQueue() {
    whenever(sqsClient.getQueueUrl(queueName)).thenReturn(someGetQueueUrlResult())
    whenever(sqsClient.getQueueAttributes(someGetQueueAttributesRequest())).thenReturn(
      someGetQueueAttributesResultWithDLQ(),
    )
    whenever(sqsDlqClient.getQueueUrl(dlqName)).thenReturn(someGetQueueUrlResultForDLQ())
    whenever(sqsDlqClient.getQueueAttributes(someGetQueueAttributesRequestForDLQ())).thenReturn(
      someGetQueueAttributesResultForDLQ(),
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

  private fun someGetQueueAttributesResultWithDLQ() = GetQueueAttributesResult().withAttributes(
    mapOf(
      "ApproximateNumberOfMessages" to "$messagesOnQueueCount",
      "ApproximateNumberOfMessagesNotVisible" to "$messagesInFlightCount",
      QueueAttributeName.RedrivePolicy.toString() to "any redrive policy",
    ),
  )

  private fun someGetQueueAttributesRequestForDLQ() =
    GetQueueAttributesRequest(dlqUrl).withAttributeNames(listOf(QueueAttributeName.All.toString()))

  private fun someGetQueueUrlResultForDLQ(): GetQueueUrlResult = GetQueueUrlResult().withQueueUrl(dlqUrl)
  private fun someGetQueueAttributesResultForDLQ() = GetQueueAttributesResult().withAttributes(
    mapOf("ApproximateNumberOfMessages" to messagesOnDLQCount.toString()),
  )
}
