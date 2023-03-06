package uk.gov.gdx.datashare.integration

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.jdbc.JdbcTestUtils
import uk.gov.gdx.datashare.helpers.JwtAuthHelper
import uk.gov.gdx.datashare.helpers.TestBase
import uk.gov.gdx.datashare.integration.wiremock.OAuthMockServer
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.integration.wiremock.PrisonerSearchApi

@ActiveProfiles("test")
abstract class IntegrationTestBase : TestBase() {
  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthHelper

  @Autowired
  private lateinit var jdbcTemplate: JdbcTemplate

  init {
    // Resolves an issue where Wiremock keeps previous sockets open from other tests causing connection resets
    System.setProperty("http.keepAlive", "false")
  }

  protected fun setAuthorisation(
    user: String = "a-client",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf(),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisation(user, roles, scopes)

  companion object {
    @JvmField
    val OauthMockServer = OAuthMockServer()

    @JvmField
    val PrisonerSearchApi = PrisonerSearchApi()

    @BeforeAll
    @JvmStatic
    fun startMocks() {
      OauthMockServer.start()
      PrisonerSearchApi.start()
      OauthMockServer.stubGrantToken()
      OauthMockServer.stubOpenId()
    }

    @AfterAll
    @JvmStatic
    fun stopMocks() {
      PrisonerSearchApi.stop()
      OauthMockServer.stop()
    }
  }

  @BeforeEach
  fun clean() {
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "acquirer_event", "acquirer_event_audit", "supplier_event")
  }
}
