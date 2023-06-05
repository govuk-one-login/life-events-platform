package uk.gov.gdx.datashare.uk.gov.gdx.datashare.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import uk.gov.gdx.datashare.models.EventNotification
import uk.gov.gdx.datashare.repositories.AcquirerEvent
import uk.gov.gdx.datashare.repositories.AcquirerEventRepository
import uk.gov.gdx.datashare.repositories.AcquirerSubscription
import uk.gov.gdx.datashare.repositories.AcquirerSubscriptionRepository
import uk.gov.gdx.datashare.services.AcquirerEventAuditService
import uk.gov.gdx.datashare.services.AcquirerEventProcessor
import uk.gov.gdx.datashare.services.AcquirerEventService
import uk.gov.gdx.datashare.services.OutboundEventQueueService
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.helpers.builders.AcquirerEventBuilder
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.helpers.builders.AcquirerSubscriptionBuilder
import java.util.*

class AcquirerEventProcessorTest {
  private val objectMapper = mockk<ObjectMapper>()
  private val acquirerSubscriptionRepository = mockk<AcquirerSubscriptionRepository>()
  private val acquirerEventRepository = mockk<AcquirerEventRepository>()
  private val acquirerEventService = mockk<AcquirerEventService>()
  private val acquirerEventAuditService = mockk<AcquirerEventAuditService>()
  private val outboundEventQueueService = mockk<OutboundEventQueueService>()

  @Test
  fun `pushes event to outbound queue`() {
    val acquirerSubscription = AcquirerSubscriptionBuilder().also {
      it.queueName = "acq_test-queue"
    }.build()
    val acquirerEvent = AcquirerEventBuilder().also {
      it.acquirerSubscriptionId = acquirerSubscription.id
      it.new = false
    }.build()
    every { acquirerSubscriptionRepository.findByAcquirerSubscriptionIdAndQueueNameIsNotNull(any<UUID>()) }
      .returns(acquirerSubscription)
    every { acquirerEventRepository.findByIdOrNull(acquirerEvent.id) }.returns(acquirerEvent)
    every { acquirerEventRepository.save(any<AcquirerEvent>()) }.returns(acquirerEvent)
    every {
      acquirerEventService.buildEnrichedEventNotification(
        any<AcquirerEvent>(),
        any<AcquirerSubscription>(),
      )
    }.returns(mockk<EventNotification>())
    every {
      acquirerEventAuditService.auditQueuedEventMessage(
        any<EventNotification>(),
        any<String>(),
      )
    }.returns(Unit)
    every { objectMapper.writeValueAsString(any<EventNotification>()) }.returns("")
    every { outboundEventQueueService.sendMessage(any<String>(), any<String>(), any<String>()) }.returns(Unit)

    val underTest = AcquirerEventProcessor(
      objectMapper,
      acquirerSubscriptionRepository,
      acquirerEventRepository,
      acquirerEventService,
      acquirerEventAuditService,
      outboundEventQueueService,
    )

    underTest.onAcquirerEvent(acquirerEvent.id.toString())

    verify(exactly = 1) {
      acquirerEventRepository.save(
        withArg<AcquirerEvent> {
          assertThat(it.id).isEqualTo(acquirerEvent.id)
          assertNotNull(it.deletedAt)
          assertThat(it.supplierEventId).isEqualTo(acquirerEvent.supplierEventId)
          assertThat(it.acquirerSubscriptionId).isEqualTo(acquirerEvent.acquirerSubscriptionId)
          assertThat(it.dataId).isEqualTo(acquirerEvent.dataId)
          assertThat(it.eventTime).isEqualTo(acquirerEvent.eventTime)
          assertThat(it.createdAt).isEqualTo(acquirerEvent.createdAt)
        },
      )
    }
    verify(exactly = 1) {
      acquirerEventAuditService.auditQueuedEventMessage(any<EventNotification>(), "acq_test-queue")
    }
    verify(exactly = 1) {
      outboundEventQueueService.sendMessage("acq_test-queue", any<String>(), any<String>())
    }
  }
}
