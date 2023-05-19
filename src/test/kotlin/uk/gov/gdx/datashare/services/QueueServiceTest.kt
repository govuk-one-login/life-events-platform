package uk.gov.gdx.datashare.uk.gov.gdx.datashare.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.DeleteQueueRequest
import software.amazon.awssdk.services.sqs.model.DeleteQueueResponse
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest
import uk.gov.gdx.datashare.services.QueueService

class QueueServiceTest {
  private val underTest: QueueService = QueueService()

  private val queueName = "MockQueue"
  private val queueUrl = "MockUrl"

  private val mockSqsClient = mockk<SqsClient>()

  @BeforeEach
  fun init() {
    mockkStatic(SqsClient::class)
    every { SqsClient.builder().build() } returns mockSqsClient
  }

  @Test
  fun `deleteQueue calls delete`() {
    every { mockSqsClient.deleteQueue(any<DeleteQueueRequest>()) } returns mockk<DeleteQueueResponse>()
    every { mockSqsClient.getQueueUrl(any<GetQueueUrlRequest>()).queueUrl() } returns queueUrl

    underTest.deleteQueue(queueName)

    verify(exactly = 1) {
      mockSqsClient.deleteQueue(
        withArg<DeleteQueueRequest> {
          Assertions.assertThat(it.queueUrl()).isEqualTo(queueUrl)
        },
      )
    }
  }
}
