package uk.gov.gdx.datashare.services

import com.amazonaws.xray.spring.aop.XRayEnabled
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.relational.core.conversion.DbActionExecutionException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.interceptor.TransactionInterceptor
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry
import uk.gov.gdx.datashare.config.SupplierSubscriptionNotFoundException
import uk.gov.gdx.datashare.queue.AwsQueue
import uk.gov.gdx.datashare.queue.AwsQueueService
import uk.gov.gdx.datashare.repositories.*

@Service
@XRayEnabled
class SupplierEventProcessor(
  private val objectMapper: ObjectMapper,
  private val supplierSubscriptionRepository: SupplierSubscriptionRepository,
  private val acquirerSubscriptionRepository: AcquirerSubscriptionRepository,
  private val supplierEventRepository: SupplierEventRepository,
  private val acquirerEventRepository: AcquirerEventRepository,
  private val awsQueueService: AwsQueueService,
) {
  private val acquirerEventQueue by lazy { awsQueueService.findByQueueId("acquirerevent") as AwsQueue }
  private val acquirerEventSqsClient by lazy { acquirerEventQueue.sqsClient }
  private val acquirerEventQueueUrl by lazy { acquirerEventQueue.queueUrl }

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @JmsListener(destination = "supplierevent", containerFactory = "awsQueueContainerFactoryProxy")
  @Transactional
  fun onSupplierEvent(message: String) {
    val supplierEvent = buildSupplierEvent(message)
    val supplierSubscription = fetchSupplierSubscription(supplierEvent)
    
    val savepoint = TransactionInterceptor.currentTransactionStatus().createSavepoint()
    try {
      supplierEventRepository.save(supplierEvent)
    }
    catch (e: DbActionExecutionException) {
      if (e.cause is DuplicateKeyException) {
        log.info("Duplicate ID for supplier event found")
        TransactionInterceptor.currentTransactionStatus().rollbackToSavepoint(savepoint)
        return
      }
    }

    val acquirerSubscriptions = acquirerSubscriptionRepository.findAllByEventType(supplierSubscription.eventType)
    val acquirerEvents = buildAndPersistAcquirerEvents(acquirerSubscriptions, supplierEvent)

    enqueueAcquirerEvents(
      acquirerEventsToPushToOutboundQueues(acquirerSubscriptions, acquirerEvents),
    )
  }

  private fun buildSupplierEvent(message: String): SupplierEvent {
    return objectMapper.readValue(message, SupplierEvent::class.java)
  }

  private fun fetchSupplierSubscription(supplierEvent: SupplierEvent) =
    (
      supplierSubscriptionRepository.findByIdOrNull(supplierEvent.supplierSubscriptionId)
        ?: throw SupplierSubscriptionNotFoundException(supplierEvent.supplierSubscriptionId)
      )

  private fun buildAndPersistAcquirerEvents(
    acquirerSubscriptions: Iterable<AcquirerSubscription>,
    supplierEvent: SupplierEvent,
  ): Iterable<AcquirerEvent> {
    val acquirerEvents = acquirerSubscriptions.map { acquirerSubscription ->
      AcquirerEvent(
        supplierEventId = supplierEvent.id,
        dataId = supplierEvent.dataId,
        eventTime = supplierEvent.eventTime,
        acquirerSubscriptionId = acquirerSubscription.id,
      )
    }

    return acquirerEventRepository.saveAll(acquirerEvents)
  }

  private fun acquirerEventsToPushToOutboundQueues(
    acquirerSubscriptions: Iterable<AcquirerSubscription>,
    acquirerEvents: Iterable<AcquirerEvent>,
  ): Iterable<AcquirerEvent> {
    val acquirerSubscriptionsById = acquirerSubscriptions.associateBy { it.acquirerSubscriptionId }
    return acquirerEvents.filter { acquirerSubscriptionsById[it.acquirerSubscriptionId]?.queueName != null }
  }

  private fun enqueueAcquirerEvents(acquirerEvents: Iterable<AcquirerEvent>) {
    val acquirerEventMessages = acquirerEvents
      .map {
        SendMessageBatchRequestEntry.builder()
          .id(it.id.toString())
          .messageBody(it.id.toString())
          .delaySeconds(10) // transaction is not committed until all messages are sent. Wait until visible in other transactions
          .build()
      }

    acquirerEventMessages.chunked(10) {
      val req = SendMessageBatchRequest.builder()
        .queueUrl(acquirerEventQueueUrl)
        .entries(it)
        .build()
      acquirerEventSqsClient.sendMessageBatch(req)
    }
  }
}
