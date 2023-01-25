package uk.gov.gdx.datashare.controller

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.repository.EventData
import uk.gov.gdx.datashare.repository.EventDataRepository
import java.util.*

class AdminControllerTest {
  private val eventDataRepository = mockk<EventDataRepository>()

  private val underTest = AdminController(eventDataRepository)

  @Test
  fun `getEvents gets events`() {
    val events = listOf(
      EventData(
        consumerSubscriptionId = UUID.randomUUID(),
        datasetId = UUID.randomUUID().toString(),
        dataId = "HMPO",
      ),
      EventData(
        consumerSubscriptionId = UUID.randomUUID(),
        datasetId = UUID.randomUUID().toString(),
        dataId = "HMPO",
      ),
    )

    every { eventDataRepository.findAll() }.returns(events)

    val eventsOutput = underTest.getEvents()

    assertThat(eventsOutput).hasSize(2)
    assertThat(eventsOutput).isEqualTo(events.toList())
  }
}
