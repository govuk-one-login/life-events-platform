package uk.gov.gdx.datashare.services

import com.amazonaws.xray.spring.aop.XRayEnabled
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.gdx.datashare.config.AcquirerSubscriptionNotFoundException
import uk.gov.gdx.datashare.repositories.AcquirerEvent
import uk.gov.gdx.datashare.repositories.AcquirerEventRepository
import uk.gov.gdx.datashare.repositories.AcquirerSubscription
import uk.gov.gdx.datashare.repositories.AcquirerSubscriptionRepository
import java.time.LocalDateTime

@Service
@XRayEnabled
class AcquirerEventProcessor(
  private val objectMapper: ObjectMapper,
  private val acquirerSubscriptionRepository: AcquirerSubscriptionRepository,
  private val acquirerEventRepository: AcquirerEventRepository,
  private val acquirerEventService: AcquirerEventService,
  private val acquirerEventAuditService: AcquirerEventAuditService,
  private val outboundEventQueueService: OutboundEventQueueService,
) {

  @JmsListener(destination = "acquirerevent", containerFactory = "awsQueueContainerFactoryProxy")
  @Transactional
  fun onAcquirerEvent(message: String) {
    val acquirerEvent = objectMapper.readValue(message, AcquirerEvent::class.java)
    val acquirerSubscription = acquirerSubscriptionRepository.findByIdOrNull(acquirerEvent.acquirerSubscriptionId)
      ?: throw AcquirerSubscriptionNotFoundException(acquirerEvent.acquirerSubscriptionId)

    if (acquirerSubscription.queueName != null) {
      persistEventAndPushToQueue(acquirerEvent, acquirerSubscription)
    } else {
      persistEvent(acquirerEvent)
    }
  }

  private fun persistEventAndPushToQueue(acquirerEvent: AcquirerEvent, acquirerSubscription: AcquirerSubscription) {
    acquirerEvent.deletedAt = LocalDateTime.now()
    acquirerEventRepository.save(acquirerEvent)
    val eventNotification = acquirerEventService.buildEnrichedEventNotification(acquirerEvent, acquirerSubscription)
    acquirerEventAuditService.auditQueuedEventMessage(eventNotification, acquirerSubscription.queueName!!)
    outboundEventQueueService.sendMessage(acquirerSubscription.queueName, eventNotification.toJson())
  }

  private fun persistEvent(acquirerEvent: AcquirerEvent) {
    acquirerEventRepository.save(acquirerEvent)
  }

  private fun Any.toJson() = objectMapper.writeValueAsString(this)
}
