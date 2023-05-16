package uk.gov.gdx.datashare.uk.gov.gdx.datashare.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
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
  private val acquirerEventRepository = mockk<AcquirerEventRepository>()
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
    acquirerEventRepository,
    awsQueueService,
  )

  companion object {
    val currentTime: LocalDateTime = LocalDateTime.of(2018, 5, 3, 1, 4)

    @JvmStatic
    @BeforeAll
    fun setUp() {
      mockkStatic(LocalDateTime::class)
      every { LocalDateTime.now() } returns currentTime
    }

    @JvmStatic
    @AfterAll
    fun tearDown() {
      unmockkStatic(LocalDateTime::class)
    }
  }

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
        queueName = "queuename2",
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
    val events = slot<List<AcquirerEvent>>()
    every { acquirerEventRepository.saveAll(capture(events)) } answers { events.captured }
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
      acquirerEventRepository.saveAll(
        withArg<List<AcquirerEvent>> {
          assertThat(it).hasSize(3)
          it.forEach { event ->
            Assertions.assertNull(event.deletedAt)
            assertThat(event.supplierEventId).isEqualTo(supplierEvent.id)
            assertThat(event.dataId).isEqualTo(supplierEvent.dataId)
            assertThat(event.eventTime).isEqualTo(supplierEvent.eventTime)
            assertThat(event.createdAt).isEqualTo(currentTime)
          }
          val receivedAcquirerSubscriptionIds = it.map { event -> event.acquirerSubscriptionId }
          val expectedAcquirerSubscriptionIds = acquirerSubscriptions.map { sub -> sub.id }
          assertThat(receivedAcquirerSubscriptionIds).isEqualTo(expectedAcquirerSubscriptionIds)
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
