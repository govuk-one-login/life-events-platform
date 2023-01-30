package uk.gov.gdx.datashare.controllers

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.enums.CognitoClientType
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.models.CognitoClientRequest
import uk.gov.gdx.datashare.models.CognitoClientResponse
import uk.gov.gdx.datashare.models.CreateAcquirerRequest
import uk.gov.gdx.datashare.repositories.EventData
import uk.gov.gdx.datashare.repositories.EventDataRepository
import uk.gov.gdx.datashare.services.AdminService
import java.util.*

class AdminControllerTest {
  private val eventDataRepository = mockk<EventDataRepository>()
  private val adminService = mockk<AdminService>()

  private val underTest = AdminController(eventDataRepository, adminService)

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

  @Test
  fun `createCognitoClient calls createCognitoClient`() {
    val cognitoClientRequest = CognitoClientRequest("HMPO", listOf(CognitoClientType.ACQUIRER))
    val cognitoClientResponse = CognitoClientResponse("HMPO", "ClientId", "ClientSecret")

    every { adminService.createCognitoClient(cognitoClientRequest) }.returns(cognitoClientResponse)

    val cognitoClientResponseOutput = underTest.createCognitoClient(cognitoClientRequest)

    verify(exactly = 1) { adminService.createCognitoClient(cognitoClientRequest) }
    assertThat(cognitoClientResponseOutput).isEqualTo(cognitoClientResponse)
  }

  @Test
  fun `createAcquirer calls createAcquirer`() {
    val createAcquirerRequest = CreateAcquirerRequest(
      "HMPO",
      EventType.DEATH_NOTIFICATION,
      listOf("firstNames", "lastName"),
      false
    )
    val cognitoClientResponse = CognitoClientResponse("HMPO", "ClientId", "ClientSecret")

    every { adminService.createAcquirer(createAcquirerRequest) }.returns(cognitoClientResponse)

    val cognitoClientResponseOutput = underTest.createAcquirer(createAcquirerRequest)

    verify(exactly = 1) { adminService.createAcquirer(createAcquirerRequest) }
    assertThat(cognitoClientResponseOutput).isEqualTo(cognitoClientResponse)
  }
}
