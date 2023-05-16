package uk.gov.gdx.datashare.uk.gov.gdx.datashare.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.LambdaClientBuilder
import software.amazon.awssdk.services.lambda.model.InvokeRequest
import software.amazon.awssdk.services.lambda.model.InvokeResponse
import uk.gov.gdx.datashare.config.JacksonConfiguration
import uk.gov.gdx.datashare.enums.GroSex
import uk.gov.gdx.datashare.models.GroDeathRecord
import uk.gov.gdx.datashare.models.GroEnrichEventResponse
import uk.gov.gdx.datashare.services.LambdaService
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
  private val mockLambdaClientBuilder = mockk<LambdaClientBuilder>()

  @BeforeEach
  fun init() {
    mockkStatic(LambdaClient::class)
    every { LambdaClient.builder() } returns mockLambdaClientBuilder
    every { mockLambdaClientBuilder.build() } returns mockLambdaClient
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
  fun `parseLambdaResponse correctly parses GroEnrichEventResponse response`() {
    val eventResponse = GroEnrichEventResponse(
      statusCode = 200,
      event = GroDeathRecord(
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
    assertThat(response.event?.registrationId).isEqualTo(eventResponse.event?.registrationId)
    assertThat(response.event?.eventTime).isEqualTo(eventResponse.event?.eventTime)
    assertThat(response.event?.verificationLevel).isEqualTo(eventResponse.event?.verificationLevel)
    assertThat(response.event?.dateOfDeath).isEqualTo(eventResponse.event?.dateOfDeath)
    assertThat(response.event?.partialMonthOfDeath).isEqualTo(eventResponse.event?.partialMonthOfDeath)
    assertThat(response.event?.partialYearOfDeath).isEqualTo(eventResponse.event?.partialYearOfDeath)
    assertThat(response.event?.forenames).isEqualTo(eventResponse.event?.forenames)
    assertThat(response.event?.surname).isEqualTo(eventResponse.event?.surname)
    assertThat(response.event?.maidenSurname).isEqualTo(eventResponse.event?.maidenSurname)
    assertThat(response.event?.sex).isEqualTo(eventResponse.event?.sex)
    assertThat(response.event?.dateOfBirth).isEqualTo(eventResponse.event?.dateOfBirth)
    assertThat(response.event?.addressLine1).isEqualTo(eventResponse.event?.addressLine1)
    assertThat(response.event?.addressLine2).isEqualTo(eventResponse.event?.addressLine2)
    assertThat(response.event?.addressLine3).isEqualTo(eventResponse.event?.addressLine3)
    assertThat(response.event?.addressLine4).isEqualTo(eventResponse.event?.addressLine4)
    assertThat(response.event?.postcode).isEqualTo(eventResponse.event?.postcode)
  }
}
