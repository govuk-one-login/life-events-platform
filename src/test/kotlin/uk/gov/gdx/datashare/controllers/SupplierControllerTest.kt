package uk.gov.gdx.datashare.uk.gov.gdx.datashare.controllers

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.controllers.SupplierController
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.models.EventToPublish
import uk.gov.gdx.datashare.services.DataReceiverService
import java.time.LocalDateTime

class SupplierControllerTest {
  private val dataReceiverService = mockk<DataReceiverService>()
  private val meterRegistry = mockk<MeterRegistry>()
  private val publishEventCounter = mockk<Counter>()

  private val underTest: SupplierController

  init {
    every { meterRegistry.counter("API_CALLS.PublishEvent", *anyVararg()) }.returns(publishEventCounter)

    every { publishEventCounter.increment() }.returns(Unit)

    underTest = SupplierController(dataReceiverService, meterRegistry)
  }

  @Test
  fun `publishEvent sends event to processor`() {
    val event = EventToPublish(
      eventType = EventType.DEATH_NOTIFICATION,
      eventTime = LocalDateTime.now(),
      id = "123456789",
    )

    every { dataReceiverService.sendToDataProcessor(any()) }.returns(Unit)

    underTest.publishEvent(event)

    verify(exactly = 1) { publishEventCounter.increment() }
    verify(exactly = 1) { dataReceiverService.sendToDataProcessor(event) }
  }
}
