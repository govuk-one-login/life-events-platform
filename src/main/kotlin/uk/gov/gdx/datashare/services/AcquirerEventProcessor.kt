package uk.gov.gdx.datashare.services

import com.amazonaws.xray.spring.aop.XRayEnabled
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.gdx.datashare.config.AcquirerSubscriptionNotFoundException
import uk.gov.gdx.datashare.config.EventNotFoundException
import uk.gov.gdx.datashare.repositories.AcquirerEvent
import uk.gov.gdx.datashare.repositories.AcquirerEventRepository
import uk.gov.gdx.datashare.repositories.AcquirerSubscription
import uk.gov.gdx.datashare.repositories.AcquirerSubscriptionRepository
import java.time.LocalDateTime
import java.util.*

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
  fun onAcquirerEvent(acquirerEventId: String) {
    val acquirerEvent = acquirerEventRepository.findByIdOrNull(UUID.fromString(acquirerEventId))
      ?: throw EventNotFoundException("No acquirer event found with ID $acquirerEventId")

    val acquirerSubscription = acquirerSubscriptionRepository.findByAcquirerSubscriptionIdAndQueueNameIsNotNull(acquirerEvent.acquirerSubscriptionId)
      ?: throw AcquirerSubscriptionNotFoundException(acquirerEvent.acquirerSubscriptionId)

    consumeEventAndPushToQueue(acquirerEvent, acquirerSubscription)
  }

  private fun consumeEventAndPushToQueue(acquirerEvent: AcquirerEvent, acquirerSubscription: AcquirerSubscription) {
    acquirerEvent.deletedAt = LocalDateTime.now()
    acquirerEventRepository.save(acquirerEvent)
    val eventNotification = acquirerEventService.buildEnrichedEventNotification(acquirerEvent, acquirerSubscription)
    acquirerEventAuditService.auditQueuedEventMessage(eventNotification, acquirerSubscription.queueName!!)
    outboundEventQueueService.sendMessage(acquirerSubscription.queueName, eventNotification.toJson())
  }

  private fun Any.toJson() = objectMapper.writeValueAsString(this)
}
