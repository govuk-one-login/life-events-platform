package uk.gov.gdx.datashare.controllers

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.models.CognitoClientResponse
import uk.gov.gdx.datashare.models.CreateAcquirerRequest
import uk.gov.gdx.datashare.models.CreateSupplierRequest
import uk.gov.gdx.datashare.services.AdminService

class AdminControllerTest {
  private val adminService = mockk<AdminService>()

  private val underTest = AdminController(adminService)

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
