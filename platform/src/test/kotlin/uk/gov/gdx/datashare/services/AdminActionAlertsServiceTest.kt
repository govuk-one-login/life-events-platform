package uk.gov.gdx.datashare.uk.gov.gdx.datashare.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.PublishResponse
import uk.gov.gdx.datashare.services.AdminAction
import uk.gov.gdx.datashare.services.AdminActionAlertsService

class AdminActionAlertsServiceTest {
  private val objectMapper = mockk<ObjectMapper>()
  private val snsClient = mockk<SnsClient>()

  private val underTest = AdminActionAlertsService(
    "topicArn",
    "test",
    objectMapper,
  )

  @Test
  fun `noticeAction reports actions to SNS`() {
    mockkStatic(SnsClient::class)
    every { SnsClient.create() } returns snsClient
    every { snsClient.publish(ofType(PublishRequest::class)) } returns mockk<PublishResponse>()
    val principal = TestingAuthenticationToken("test principal", "")
    SecurityContextHolder.getContext().authentication = principal
    every { objectMapper.writeValueAsString(any()) } returns "{details}"

    underTest.noticeAction(AdminAction("test action", object {}))

    verify(exactly = 1) {
      snsClient.publish(
        withArg<PublishRequest> {
          assertThat(it.message()).isEqualTo("test principal performed test action, environment: test, details: {details}")
          assertThat(it.topicArn()).isEqualTo("topicArn")
        },
      )
    }
  }
}
