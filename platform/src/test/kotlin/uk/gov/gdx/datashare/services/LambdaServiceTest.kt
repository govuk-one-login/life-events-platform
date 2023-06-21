package uk.gov.gdx.datashare.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.InvokeRequest
import software.amazon.awssdk.services.lambda.model.InvokeResponse
import uk.gov.gdx.datashare.config.JacksonConfiguration
import uk.gov.gdx.datashare.enums.GroSex
import uk.gov.gdx.datashare.models.GroDeathRecord
import uk.gov.gdx.datashare.models.GroDeleteEventResponse
import uk.gov.gdx.datashare.models.GroEnrichEventResponse
import java.time.LocalDate

class LambdaServiceTest {
  private val objectMapper = JacksonConfiguration().objectMapper()

  private val underTest: LambdaService = LambdaService(objectMapper)

  private val functionName = "MockFunction"
  private val jsonPayload = objectMapper.writeValueAsString(
    object {
      val id = "ID"
    },
  )

  private val mockLambdaClient = mockk<LambdaClient>()

  @BeforeEach
  fun init() {
    mockkStatic(LambdaClient::class)
    every { LambdaClient.builder().build() } returns mockLambdaClient
  }

  @Test
  fun `invokeLambda calls invoke`() {
    every { mockLambdaClient.invoke(any<InvokeRequest>()) } returns mockk<InvokeResponse>()

    underTest.invokeLambda(functionName, jsonPayload)

    verify(exactly = 1) {
      mockLambdaClient.invoke(
        withArg<InvokeRequest> {
          assertThat(it.functionName()).isEqualTo(functionName)
          assertThat(it.payload()).isEqualTo(SdkBytes.fromUtf8String(jsonPayload))
        },
      )
    }
  }

  @Test
  fun `parseLambdaResponse correctly parses number response`() {
    val intJsonPayload = objectMapper.writeValueAsString(1)
    val mockInvokeResponse = mockk<InvokeResponse>()
    every { mockInvokeResponse.payload() } returns SdkBytes.fromUtf8String(intJsonPayload)

    val response = underTest.parseLambdaResponse(mockInvokeResponse, Number::class.java)

    assertThat(response).isEqualTo(1)
  }

  @Test
  fun `parseLambdaResponse correctly parses GroDeleteEventResponse response`() {
    val eventResponse = GroDeleteEventResponse(
      statusCode = 200,
      payload = "asdfasdfasdfasdfasdf",
    )
    val eventJsonPayload = objectMapper.writeValueAsString(eventResponse)
    val mockInvokeResponse = mockk<InvokeResponse>()
    every { mockInvokeResponse.payload() } returns SdkBytes.fromUtf8String(eventJsonPayload)

    val response = underTest.parseLambdaResponse(mockInvokeResponse, GroDeleteEventResponse::class.java)

    assertThat(response.statusCode).isEqualTo(eventResponse.statusCode)
    assertThat(response.payload).isEqualTo(eventResponse.payload)
  }

  @Test
  fun `parseLambdaResponse correctly parses GroEnrichEventResponse response`() {
    val eventResponse = GroEnrichEventResponse(
      statusCode = 200,
      payload = GroDeathRecord(
        hash = "asdfasdfasdfasdfasdf",
        registrationId = "registrationId",
        eventTime = LocalDate.now(),
        verificationLevel = "1",
        dateOfDeath = LocalDate.now().minusDays(100),
        partialMonthOfDeath = "2",
        partialYearOfDeath = "2020",
        forenames = "forenames",
        surname = "surname",
        maidenSurname = "maidenSurname",
        sex = GroSex.FEMALE,
        dateOfBirth = LocalDate.now().minusDays(1000),
        addressLine1 = "addressLine1",
        addressLine2 = "addressLine2",
        addressLine3 = "addressLine3",
        addressLine4 = "addressLine4",
        postcode = "postcode",
      ),
    )
    val eventJsonPayload = objectMapper.writeValueAsString(eventResponse)
    val mockInvokeResponse = mockk<InvokeResponse>()
    every { mockInvokeResponse.payload() } returns SdkBytes.fromUtf8String(eventJsonPayload)

    val response = underTest.parseLambdaResponse(mockInvokeResponse, GroEnrichEventResponse::class.java)

    assertThat(response.statusCode).isEqualTo(eventResponse.statusCode)
    assertThat(response.payload?.registrationId).isEqualTo(eventResponse.payload?.registrationId)
    assertThat(response.payload?.eventTime).isEqualTo(eventResponse.payload?.eventTime)
    assertThat(response.payload?.verificationLevel).isEqualTo(eventResponse.payload?.verificationLevel)
    assertThat(response.payload?.dateOfDeath).isEqualTo(eventResponse.payload?.dateOfDeath)
    assertThat(response.payload?.partialMonthOfDeath).isEqualTo(eventResponse.payload?.partialMonthOfDeath)
    assertThat(response.payload?.partialYearOfDeath).isEqualTo(eventResponse.payload?.partialYearOfDeath)
    assertThat(response.payload?.forenames).isEqualTo(eventResponse.payload?.forenames)
    assertThat(response.payload?.surname).isEqualTo(eventResponse.payload?.surname)
    assertThat(response.payload?.maidenSurname).isEqualTo(eventResponse.payload?.maidenSurname)
    assertThat(response.payload?.sex).isEqualTo(eventResponse.payload?.sex)
    assertThat(response.payload?.dateOfBirth).isEqualTo(eventResponse.payload?.dateOfBirth)
    assertThat(response.payload?.addressLine1).isEqualTo(eventResponse.payload?.addressLine1)
    assertThat(response.payload?.addressLine2).isEqualTo(eventResponse.payload?.addressLine2)
    assertThat(response.payload?.addressLine3).isEqualTo(eventResponse.payload?.addressLine3)
    assertThat(response.payload?.addressLine4).isEqualTo(eventResponse.payload?.addressLine4)
    assertThat(response.payload?.postcode).isEqualTo(eventResponse.payload?.postcode)
  }
}
