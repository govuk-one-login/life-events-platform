package uk.gov.gdx.datashare.controllers

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.repositories.EventData
import uk.gov.gdx.datashare.repositories.EventDataRepository
import java.util.*

class AdminControllerTest {
  private val eventDataRepository = mockk<EventDataRepository>()

  private val underTest = AdminController(eventDataRepository)

  @Test
  fun `getEvents gets events`() {
    val events = listOf(
      EventData(
        consumerSubscriptionId = UUID.randomUUID(),
        dataId = "HMPO",
      ),
      EventData(
        consumerSubscriptionId = UUID.randomUUID(),
        dataId = "HMPO",
      ),
    )

    every { eventDataRepository.findAll() }.returns(events)

    val eventsOutput = underTest.getEvents()

    assertThat(eventsOutput).hasSize(2)
    assertThat(eventsOutput).isEqualTo(events.toList())
  }
}
