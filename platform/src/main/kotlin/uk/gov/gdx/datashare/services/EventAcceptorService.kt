package uk.gov.gdx.datashare.services

import com.amazonaws.xray.spring.aop.XRayEnabled
import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.gdx.datashare.config.AuthenticationFacade
import uk.gov.gdx.datashare.config.DateTimeHandler
import uk.gov.gdx.datashare.config.SupplierPermissionException
import uk.gov.gdx.datashare.models.EventToPublish
import uk.gov.gdx.datashare.queue.AwsQueue
import uk.gov.gdx.datashare.queue.AwsQueueService
import uk.gov.gdx.datashare.repositories.SupplierEvent
import uk.gov.gdx.datashare.repositories.SupplierSubscriptionRepository

@Service
@XRayEnabled
class EventAcceptorService(
  private val awsQueueService: AwsQueueService,
  private val authenticationFacade: AuthenticationFacade,
  private val supplierSubscriptionRepository: SupplierSubscriptionRepository,
  private val objectMapper: ObjectMapper,
  private val dateTimeHandler: DateTimeHandler,
  private val meterRegistry: MeterRegistry,
) {
  private val supplierEventQueue by lazy { awsQueueService.findByQueueId("supplierevent") as AwsQueue }
  private val supplierEventSqsClient by lazy { supplierEventQueue.sqsClient }
  private val supplierEventQueueUrl by lazy { supplierEventQueue.queueUrl }

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun acceptEvent(eventPayload: EventToPublish): SupplierEvent {
    return acceptEvent(eventPayload, authenticationFacade.getUsername())
  }

  fun acceptEvent(eventPayload: EventToPublish, clientId: String): SupplierEvent {
    val subscription =
      supplierSubscriptionRepository.findByClientIdAndEventTypeAndWhenDeletedIsNull(clientId, eventPayload.eventType) ?: throw SupplierPermissionException("$clientId does not have permission")

    val supplierEvent = SupplierEvent(
      supplierSubscriptionId = subscription.supplierSubscriptionId,
      dataId = eventPayload.id,
      eventTime = eventPayload.eventTime,
      createdAt = dateTimeHandler.now(),
    )

    meterRegistry.counter("EVENT_ACTION.EventPublished", "eventType", eventPayload.eventType.name).increment()

    supplierEventSqsClient.sendMessage(
      SendMessageRequest.builder().queueUrl(
        supplierEventQueueUrl,
      ).messageBody(
        supplierEvent.toJson(),
      ).build(),
    )

    return supplierEvent
  }

  private fun Any.toJson() = objectMapper.writeValueAsString(this)
}
