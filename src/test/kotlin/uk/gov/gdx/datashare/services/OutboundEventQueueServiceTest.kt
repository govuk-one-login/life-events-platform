package uk.gov.gdx.datashare.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.gdx.datashare.queue.AwsQueueFactory
import uk.gov.gdx.datashare.queue.SqsProperties

class OutboundEventQueueServiceTest {
  private val awsQueueFactory = mockk<AwsQueueFactory>()
  private val sqsProperties = mockk<SqsProperties>()
  private val sqsClient = mockk<SqsClient>(relaxed = true)
  private val secondSqsClient = mockk<SqsClient>(relaxed = true)
  private val underTest = OutboundEventQueueService(
    awsQueueFactory,
    sqsProperties,
  )

  @BeforeEach
  fun setup() {
    every { sqsClient.getQueueUrl(any<GetQueueUrlRequest>()).queueUrl() }.returns("queueurl")

    every {
      awsQueueFactory.getOrDefaultSqsClient(
        any<String>(),
        any<SqsProperties.QueueConfig>(),
        any<SqsProperties>(),
        any<SqsClient>(),
      )
    }.returns(sqsClient).andThen(secondSqsClient)
  }

  @Test
  fun `sends message`() {
    underTest.sendMessage("queueone", "message")

    verify(exactly = 1) {
      awsQueueFactory.getOrDefaultSqsClient(
        "queueone",
        any<SqsProperties.QueueConfig>(),
        any<SqsProperties>(),
        any<SqsClient>(),
      )
    }
    verify(exactly = 1) {
      sqsClient.sendMessage(any<SendMessageRequest>())
    }
  }

  @Test
  fun `memoizes clients`() {
    underTest.sendMessage("queueone", "message")
    underTest.sendMessage("queueone", "message")

    verify(exactly = 1) {
      awsQueueFactory.getOrDefaultSqsClient(
        any<String>(),
        any<SqsProperties.QueueConfig>(),
        any<SqsProperties>(),
        any<SqsClient>(),
      )
    }
    verify(exactly = 2) {
      sqsClient.sendMessage(any<SendMessageRequest>())
    }
  }

  @Test
  fun `fetches new client for every queue`() {
    underTest.sendMessage("queueone", "message")
    underTest.sendMessage("queuetwo", "message")

    verifySequence {
      awsQueueFactory.getOrDefaultSqsClient(
        "queueone",
        any<SqsProperties.QueueConfig>(),
        any<SqsProperties>(),
        any<SqsClient>(),
      )
      awsQueueFactory.getOrDefaultSqsClient(
        "queuetwo",
        any<SqsProperties.QueueConfig>(),
        any<SqsProperties>(),
        any<SqsClient>(),
      )
    }

    verify(exactly = 1) {
      sqsClient.sendMessage(any<SendMessageRequest>())
    }
    verify(exactly = 1) {
      secondSqsClient.sendMessage(any<SendMessageRequest>())
    }
  }
}
