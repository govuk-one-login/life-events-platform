package uk.gov.gdx.datashare.uk.gov.gdx.datashare.services

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import software.amazon.awssdk.services.lambda.model.InvokeResponse
import uk.gov.gdx.datashare.config.JacksonConfiguration
import uk.gov.gdx.datashare.config.NoDataFoundException
import uk.gov.gdx.datashare.enums.GroSex
import uk.gov.gdx.datashare.models.GroDeathRecord
import uk.gov.gdx.datashare.models.GroEnrichEventResponse
import uk.gov.gdx.datashare.services.GroApiService
import uk.gov.gdx.datashare.services.LambdaService
import java.time.LocalDate

class GroApiServiceTest {
  private val lambdaService = mockk<LambdaService>()
  private val objectMapper = JacksonConfiguration().objectMapper()
  private val functionName = "MockLambda"

  private val underTest: GroApiService = GroApiService(lambdaService, objectMapper, functionName)

  private val dataId = "asdfasdfasdfasdfasdf"
  private val deathRecord = GroDeathRecord(
    hash = dataId,
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
  )
  private val jsonPayload = objectMapper.writeValueAsString(
    object {
      val id = dataId
    },
  )
  private val invokeResponse = mockk<InvokeResponse>()

  @Test
  fun `enrichEvent enriches event for valid ID`() {
    every { lambdaService.invokeLambda(functionName, jsonPayload) } returns invokeResponse
    every { lambdaService.parseLambdaResponse(invokeResponse, GroEnrichEventResponse::class.java) } returns
      GroEnrichEventResponse(
        statusCode = 200,
        event = deathRecord,
      )

    val enrichedPayload = underTest.enrichEvent(dataId)

    assertThat(enrichedPayload.registrationId).isEqualTo(deathRecord.registrationId)
    assertThat(enrichedPayload.eventTime).isEqualTo(deathRecord.eventTime)
    assertThat(enrichedPayload.verificationLevel).isEqualTo(deathRecord.verificationLevel)
    assertThat(enrichedPayload.dateOfDeath).isEqualTo(deathRecord.dateOfDeath)
    assertThat(enrichedPayload.partialMonthOfDeath).isEqualTo(deathRecord.partialMonthOfDeath)
    assertThat(enrichedPayload.partialYearOfDeath).isEqualTo(deathRecord.partialYearOfDeath)
    assertThat(enrichedPayload.forenames).isEqualTo(deathRecord.forenames)
    assertThat(enrichedPayload.surname).isEqualTo(deathRecord.surname)
    assertThat(enrichedPayload.maidenSurname).isEqualTo(deathRecord.maidenSurname)
    assertThat(enrichedPayload.sex).isEqualTo(deathRecord.sex)
    assertThat(enrichedPayload.dateOfBirth).isEqualTo(deathRecord.dateOfBirth)
    assertThat(enrichedPayload.addressLine1).isEqualTo(deathRecord.addressLine1)
    assertThat(enrichedPayload.addressLine2).isEqualTo(deathRecord.addressLine2)
    assertThat(enrichedPayload.addressLine3).isEqualTo(deathRecord.addressLine3)
    assertThat(enrichedPayload.addressLine4).isEqualTo(deathRecord.addressLine4)
    assertThat(enrichedPayload.postcode).isEqualTo(deathRecord.postcode)
  }

  @Test
  fun `enrichEvent throws for not found ID`() {
    every { lambdaService.invokeLambda(functionName, jsonPayload) } returns invokeResponse
    every { lambdaService.parseLambdaResponse(invokeResponse, GroEnrichEventResponse::class.java) } returns
      GroEnrichEventResponse(
        statusCode = 401,
      )

    val exception = assertThrows<NoDataFoundException> { underTest.enrichEvent(dataId) }
    assertThat(exception.message).isEqualTo("No data found for ID $dataId")
  }

  @Test
  fun `enrichEvent throws for null lambda`() {
    val underTest = GroApiService(lambdaService, objectMapper, null)

    val exception = assertThrows<IllegalStateException> { underTest.enrichEvent(dataId) }
    assertThat(exception.message).isEqualTo("Function name not found.")
  }
}
