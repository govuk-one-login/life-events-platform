package uk.gov.gdx.datashare.integration

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParameterRequest
import software.amazon.awssdk.services.ssm.model.GetParameterResponse
import software.amazon.awssdk.services.ssm.model.Parameter
import uk.gov.gdx.datashare.helpers.JwtAuthHelper
import uk.gov.gdx.datashare.helpers.TestBase
import uk.gov.gdx.datashare.integration.wiremock.OAuthMockServer
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.integration.wiremock.PrisonerSearchApi

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
abstract class IntegrationTestBase : TestBase() {
  @MockkBean
  var ssmClient = mockk<SsmClient>().also {
    val parameter = Parameter.builder().value("mock_parameter").build()
    val getParameterResponse = GetParameterResponse.builder().parameter(parameter).build()
    every { it.getParameter(any<software.amazon.awssdk.services.ssm.model.GetParameterRequest>()) }.returns(
      getParameterResponse,
    )
  }

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthHelper

//  @MockkBean
//  fun ssmClient(): SsmClient {
//    val mockSsmClient = mockk<SsmClient>()
//    val parameter = Parameter.builder().value("mock_parameter").build()
//    val getParameterResponse = GetParameterResponse.builder().parameter(parameter).build()
//    every { mockSsmClient.getParameter(any<GetParameterRequest>()) }.returns(getParameterResponse)
//    return mockSsmClient
//  }

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
}
