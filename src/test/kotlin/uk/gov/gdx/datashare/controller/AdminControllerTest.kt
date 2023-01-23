package uk.gov.gdx.datashare.controller

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.repository.EventData
import uk.gov.gdx.datashare.repository.EventDataRepository
import java.util.UUID

class AdminControllerTest {
  private val eventDataRepository = mockk<EventDataRepository>()

  private val underTest = AdminController(eventDataRepository)

  @Test
  fun `getEvents gets events`() {
    runBlocking {
      val events = flowOf(
        EventData(
          consumerSubscriptionId = UUID.randomUUID(),
          datasetId = UUID.randomUUID().toString(),
          dataId = "HMPO",
          dataPayload = null,
        ),
        EventData(
          consumerSubscriptionId = UUID.randomUUID(),
          datasetId = UUID.randomUUID().toString(),
          dataId = "HMPO",
          dataPayload = "{\"firstName\":\"Bob\"}",
        ),
      )

      coEvery { eventDataRepository.findAll() }.returns(events)

      val eventsOutput = underTest.getEvents()

      assertThat(eventsOutput).hasSize(2)
      assertThat(eventsOutput).isEqualTo(events.toList())
    }
  }
}
