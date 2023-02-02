package uk.gov.gdx.datashare.uk.gov.gdx.datashare.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.config.JacksonConfiguration
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.models.EventToPublish
import uk.gov.gdx.datashare.repositories.SupplierSubscription
import uk.gov.gdx.datashare.repositories.SupplierSubscriptionRepository
import uk.gov.gdx.datashare.services.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class PrisonerEventMessageProcessorTest {
  private val objectMapper = JacksonConfiguration().objectMapper()
  private val dataReceiverService = mockk<DataReceiverService>()
  private val supplierSubscriptionRepository = mockk<SupplierSubscriptionRepository>()

  private val underTest: PrisonerEventMessageProcessor = PrisonerEventMessageProcessor(
    objectMapper,
    dataReceiverService,
    supplierSubscriptionRepository,
  )

  private val supplierSubscriptions = listOf(
    SupplierSubscription(
      supplierId = UUID.randomUUID(),
      eventType = EventType.ENTERED_PRISON,
      clientId = "random-client-id-1",
    ),
    SupplierSubscription(
      supplierId = UUID.randomUUID(),
      eventType = EventType.ENTERED_PRISON,
      clientId = "random-client-id-2",
    ),
  )

  private val eventPayload = EventToPublish(
    eventType = EventType.ENTERED_PRISON,
    eventTime = LocalDateTime.parse("2023-01-12T15:14:24.125533+00:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
    id = "A1234AA",
  )

  @Test
  fun `processes prisoner entering prison event`() {
    every { supplierSubscriptionRepository.findFirstByEventType(EventType.ENTERED_PRISON) }
      .returns(supplierSubscriptions[0])

    val clientId = supplierSubscriptions[0].clientId

    every {
      dataReceiverService.sendToDataProcessor(
        eventPayload,
        clientId,
      )
    }.answers { }

    underTest.onPrisonerEventMessage("/messages/prisonerReceived.json".readResourceAsText())

    verify(exactly = 1) {
      dataReceiverService.sendToDataProcessor(
        eventPayload,
        clientId,
      )
    }
  }

  @Test
  fun `processes prisoner leaving prison event`() {
    underTest.onPrisonerEventMessage("/messages/prisonerReleased.json".readResourceAsText())

    verify(exactly = 0) {
      supplierSubscriptionRepository.findFirstByEventType(
        any(),
      )
    }

    verify(exactly = 0) {
      dataReceiverService.sendToDataProcessor(
        any(),
        any(),
      )
    }
  }

  private fun String.readResourceAsText(): String {
    return PrisonerEventMessageProcessorTest::class.java.getResource(this)?.readText() ?: throw AssertionError("can not find file")
  }
}
