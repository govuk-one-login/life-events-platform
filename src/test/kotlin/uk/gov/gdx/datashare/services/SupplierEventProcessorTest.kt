package uk.gov.gdx.datashare.uk.gov.gdx.datashare.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest
import software.amazon.awssdk.services.sqs.model.SendMessageBatchResponse
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.queue.AwsQueue
import uk.gov.gdx.datashare.queue.AwsQueueService
import uk.gov.gdx.datashare.repositories.*
import uk.gov.gdx.datashare.services.SupplierEventProcessor
import java.time.LocalDateTime
import java.util.*

class SupplierEventProcessorTest {
  private val objectMapper = mockk<ObjectMapper>()
  private val supplierSubscriptionRepository = mockk<SupplierSubscriptionRepository>()
  private val acquirerSubscriptionRepository = mockk<AcquirerSubscriptionRepository>()
  private val supplierEventRepository = mockk<SupplierEventRepository>()
  private val awsQueueService = mockk<AwsQueueService>()
  private val supplierSubscription = SupplierSubscription(
    id = UUID.randomUUID(),
    supplierId = UUID.randomUUID(),
    clientId = "",
    eventType = EventType.DEATH_NOTIFICATION,
  )

  private val underTest: SupplierEventProcessor = SupplierEventProcessor(
    objectMapper,
    supplierSubscriptionRepository,
    acquirerSubscriptionRepository,
    supplierEventRepository,
    awsQueueService,
  )

  @Test
  fun `onGovEvent saves death notifications for LEV`() {
    val supplierEvent = SupplierEvent(
      id = UUID.randomUUID(),
      supplierSubscriptionId = supplierSubscription.id,
      dataId = "1234",
      eventTime = LocalDateTime.of(2010, 1, 1, 12, 0),
    )
    every { supplierSubscriptionRepository.findByIdOrNull(supplierSubscription.id) }
      .returns(supplierSubscription)
    val acquirerSubscriptions = listOf(
      AcquirerSubscription(
        acquirerId = UUID.randomUUID(),
        eventType = supplierSubscription.eventType,
        queueName = "queuename",
      ),
      AcquirerSubscription(
        acquirerId = UUID.randomUUID(),
        eventType = supplierSubscription.eventType,
        oauthClientId = "clientId",
      ),
    )
    every { acquirerSubscriptionRepository.findAllByEventType(supplierSubscription.eventType) }
      .returns(acquirerSubscriptions)
    every { supplierEventRepository.save(any<SupplierEvent>()) }.returns(mockk<SupplierEvent>())
    every { objectMapper.readValue(any<String>(), SupplierEvent::class.java) }.returns(supplierEvent)
    every { objectMapper.writeValueAsString(any<AcquirerEvent>()) }.returns("")
    val queueClient = mockQueueClient()

    underTest.onSupplierEvent("string")

    verify(exactly = 1) {
      supplierEventRepository.save(
        withArg<SupplierEvent> {
          assertThat(it.id).isEqualTo(supplierEvent.id)
          assertThat(it.dataId).isEqualTo(supplierEvent.dataId)
          assertThat(it.supplierSubscriptionId).isEqualTo(supplierSubscription.id)
          assertThat(it.eventTime).isEqualTo(supplierEvent.eventTime)
        },
      )
    }
    verify(exactly = 1) {
      queueClient.sendMessageBatch(
        withArg<SendMessageBatchRequest> {
          assertThat(it.entries()).hasSize(2)
        },
      )
    }
  }

  private fun mockQueueClient(): SqsClient {
    val awsQueue = mockk<AwsQueue>()
    val queueClient = mockk<SqsClient>()
    every { awsQueueService.findByQueueId(any<String>()) }.returns(awsQueue)
    every { awsQueue.sqsClient }.returns(queueClient)
    every { awsQueue.queueUrl }.returns("")
    every { queueClient.sendMessageBatch(any<SendMessageBatchRequest>()) }.returns(mockk<SendMessageBatchResponse>())
    return queueClient
  }
}
