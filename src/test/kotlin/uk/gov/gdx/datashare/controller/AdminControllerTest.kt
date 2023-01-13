package uk.gov.gdx.datashare.controller

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.repository.EgressEventData
import uk.gov.gdx.datashare.repository.EgressEventDataRepository
import uk.gov.gdx.datashare.repository.IngressEventData
import uk.gov.gdx.datashare.repository.IngressEventDataRepository
import java.util.UUID

class AdminControllerTest {
  private val egressEventDataRepository = mockk<EgressEventDataRepository>()
  private val ingressEventDataRepository = mockk<IngressEventDataRepository>()

  private val underTest = AdminController(egressEventDataRepository, ingressEventDataRepository)

  @Test
  fun `getEgressEvents gets egress events`() {
    runBlocking {
      val egressEvents = flowOf(
        EgressEventData(
          consumerSubscriptionId = UUID.randomUUID(),
          ingressEventId = UUID.randomUUID(),
          datasetId = UUID.randomUUID().toString(),
          dataId = "HMPO",
          dataPayload = null,
        ),
        EgressEventData(
          consumerSubscriptionId = UUID.randomUUID(),
          ingressEventId = UUID.randomUUID(),
          datasetId = UUID.randomUUID().toString(),
          dataId = "HMPO",
          dataPayload = "{\"firstName\":\"Bob\"}",
        ),
      )

      coEvery { egressEventDataRepository.findAll() }.returns(egressEvents)

      val events = underTest.getEgressEvents()

      assertThat(events).hasSize(2)
      assertThat(events).isEqualTo(egressEvents.toList())
    }
  }

  @Test
  fun `getIngressEvents gets ingress events`() {
    runBlocking {
      val ingressEvents = flowOf(
        IngressEventData(
          eventTypeId = "DEATH_NOTIFICATION",
          subscriptionId = UUID.randomUUID(),
          datasetId = UUID.randomUUID().toString(),
          dataId = "HMPO",
          dataPayload = null,
        ),
        IngressEventData(
          eventTypeId = "DEATH_NOTIFICATION",
          subscriptionId = UUID.randomUUID(),
          datasetId = UUID.randomUUID().toString(),
          dataId = "HMPO",
          dataPayload = "{\"firstName\":\"Bob\"}",
        ),
      )

      coEvery { ingressEventDataRepository.findAll() }.returns(ingressEvents)

      val events = underTest.getIngressEvents()

      assertThat(events).hasSize(2)
      assertThat(events).isEqualTo(ingressEvents.toList())
    }
  }
}
