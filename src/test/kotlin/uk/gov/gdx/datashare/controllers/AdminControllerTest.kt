package uk.gov.gdx.datashare.controllers

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.enums.CognitoClientType
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.models.CognitoClientRequest
import uk.gov.gdx.datashare.models.CognitoClientResponse
import uk.gov.gdx.datashare.models.CreateAcquirerRequest
import uk.gov.gdx.datashare.models.CreateSupplierRequest
import uk.gov.gdx.datashare.repositories.AcquirerEvent
import uk.gov.gdx.datashare.repositories.AcquirerEventRepository
import uk.gov.gdx.datashare.services.AdminService
import java.util.*

class AdminControllerTest {
  private val acquirerEventRepository = mockk<AcquirerEventRepository>()
  private val adminService = mockk<AdminService>()

  private val underTest = AdminController(acquirerEventRepository, adminService)

  @Test
  fun `getEvents gets events`() {
    val events = listOf(
      AcquirerEvent(
        acquirerSubscriptionId = UUID.randomUUID(),
        dataId = "HMPO",
        supplierEventId = UUID.randomUUID(),
        eventTime = null,
      ),
      AcquirerEvent(
        acquirerSubscriptionId = UUID.randomUUID(),
        dataId = "HMPO",
        supplierEventId = UUID.randomUUID(),
        eventTime = null,
      ),
    )

    every { acquirerEventRepository.findAll() }.returns(events)

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
      listOf(EnrichmentField.FIRST_NAMES, EnrichmentField.LAST_NAME),
      false,
    )
    val cognitoClientResponse = CognitoClientResponse("HMPO", "ClientId", "ClientSecret")

    every { adminService.createAcquirer(createAcquirerRequest) }.returns(cognitoClientResponse)

    val cognitoClientResponseOutput = underTest.createAcquirer(createAcquirerRequest)

    verify(exactly = 1) { adminService.createAcquirer(createAcquirerRequest) }
    assertThat(cognitoClientResponseOutput).isEqualTo(cognitoClientResponse)
  }

  @Test
  fun `createSupplier calls createSupplier`() {
    val createSupplierRequest = CreateSupplierRequest(
      "HMPO",
      EventType.DEATH_NOTIFICATION,
    )
    val cognitoClientResponse = CognitoClientResponse("HMPO", "ClientId", "ClientSecret")

    every { adminService.createSupplier(createSupplierRequest) }.returns(cognitoClientResponse)

    val cognitoClientResponseOutput = underTest.createSupplier(createSupplierRequest)

    verify(exactly = 1) { adminService.createSupplier(createSupplierRequest) }
    assertThat(cognitoClientResponseOutput).isEqualTo(cognitoClientResponse)
  }
}
