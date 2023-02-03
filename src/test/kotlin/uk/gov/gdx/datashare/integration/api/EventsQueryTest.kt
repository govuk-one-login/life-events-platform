package uk.gov.gdx.datashare.integration.api

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.web.WebAppConfiguration
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.Parameter
import software.amazon.awssdk.services.ssm.model.GetParameterRequest
import software.amazon.awssdk.services.ssm.model.GetParameterResponse
import uk.gov.gdx.datashare.integration.SqsIntegrationTestBase

class EventsQueryTest : SqsIntegrationTestBase() {

  @Test
  fun `Get events successfully queries db`() {
    webTestClient.get()
      .uri("/events")
      .headers(setAuthorisation("event-query-test", listOf(""), listOf("events/consume")))
      .exchange()
      .expectStatus()
      .isOk
  }
}
