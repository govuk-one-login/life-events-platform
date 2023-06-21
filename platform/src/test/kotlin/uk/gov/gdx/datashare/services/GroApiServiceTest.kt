package uk.gov.gdx.datashare.services

import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import software.amazon.awssdk.services.lambda.model.InvokeResponse
import uk.gov.gdx.datashare.config.DateTimeHandler
import uk.gov.gdx.datashare.config.JacksonConfiguration
import uk.gov.gdx.datashare.config.NoDataFoundException
import uk.gov.gdx.datashare.enums.GroSex
import uk.gov.gdx.datashare.models.GroDeathRecord
import uk.gov.gdx.datashare.models.GroDeleteEventResponse
import uk.gov.gdx.datashare.models.GroEnrichEventResponse
import uk.gov.gdx.datashare.repositories.SupplierEvent
import uk.gov.gdx.datashare.repositories.SupplierEventRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class GroApiServiceTest {
  private val dateTimeHandler = mockk<DateTimeHandler>()
  private val lambdaService = mockk<LambdaService>()
  private val objectMapper = JacksonConfiguration().objectMapper()
  private val supplierEventRepository = mockk<SupplierEventRepository>()
  private val deleteFunctionName = "DeleteMockLambda"
  private val enrichFunctionName = "EnrichMockLambda"

  private val underTest: GroApiService = GroApiService(
    dateTimeHandler,
    lambdaService,
    objectMapper,
    supplierEventRepository,
    deleteFunctionName,
    enrichFunctionName,
  )

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
  private val supplierEvent = SupplierEvent(
    supplierSubscriptionId = UUID.randomUUID(),
    dataId = "asdasd",
    eventTime = null,
  )
  private val jsonPayload = objectMapper.writeValueAsString(
    object {
      val id = dataId
    },
  )
  private val invokeResponse = mockk<InvokeResponse>()

  @Test
  fun `deleteConsumedGroSupplierEvent deletes event`() {
    val now = LocalDateTime.now()
    every { dateTimeHandler.now() } returns now
    every { supplierEventRepository.save(any()) } returns supplierEvent
    every { lambdaService.invokeLambda(deleteFunctionName, any()) } returns invokeResponse
    every { lambdaService.parseLambdaResponse(invokeResponse, GroDeleteEventResponse::class.java) } returns
      GroDeleteEventResponse(
        statusCode = 200,
        payload = "asd",
      )

    underTest.deleteConsumedGroSupplierEvent(supplierEvent)

    verify(exactly = 1) {
      supplierEventRepository.save(
        withArg<SupplierEvent> {
          assertThat(it.deletedAt).isEqualTo(now)
          assertThat(it.id).isEqualTo(supplierEvent.id)
        },
      )
    }
    verify(exactly = 1) {
      lambdaService.invokeLambda(
        deleteFunctionName,
        objectMapper.writeValueAsString(
          object {
            val id = supplierEvent.dataId
          },
        ),
      )
    }
  }

  @Test
  fun `deleteConsumedGroSupplierEvent throws for null lambda`() {
    val now = LocalDateTime.now()
    every { dateTimeHandler.now() } returns now
    every { supplierEventRepository.findGroDeathEventsForDeletion() } returns mutableListOf(supplierEvent)

    val underTest = GroApiService(
      dateTimeHandler,
      lambdaService,
      objectMapper,
      supplierEventRepository,
      null,
      enrichFunctionName,
    )

    val exception = assertThrows<IllegalStateException> { underTest.deleteConsumedGroSupplierEvent(supplierEvent) }
    assertThat(exception.message).isEqualTo("Function name for delete not found.")

    verify(exactly = 0) {
      supplierEventRepository.save(any())
    }
    verify(exactly = 0) {
      lambdaService.invokeLambda(any(), any())
    }
  }

  @Test
  fun `enrichEvent enriches event for valid ID`() {
    every { lambdaService.invokeLambda(enrichFunctionName, jsonPayload) } returns invokeResponse
    every { lambdaService.parseLambdaResponse(invokeResponse, GroEnrichEventResponse::class.java) } returns
      GroEnrichEventResponse(
        statusCode = 200,
        payload = deathRecord,
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
    every { lambdaService.invokeLambda(enrichFunctionName, jsonPayload) } returns invokeResponse
    every { lambdaService.parseLambdaResponse(invokeResponse, GroEnrichEventResponse::class.java) } returns
      GroEnrichEventResponse(
        statusCode = 401,
      )

    val exception = assertThrows<NoDataFoundException> { underTest.enrichEvent(dataId) }
    assertThat(exception.message).isEqualTo("No data found to enrich ID $dataId")
  }

  @Test
  fun `enrichEvent throws for null lambda`() {
    val underTest = GroApiService(
      dateTimeHandler,
      lambdaService,
      objectMapper,
      supplierEventRepository,
      deleteFunctionName,
      null,
    )

    val exception = assertThrows<IllegalStateException> { underTest.enrichEvent(dataId) }
    assertThat(exception.message).isEqualTo("Function name for enrich not found.")

    verify(exactly = 0) {
      lambdaService.invokeLambda(any(), any())
    }
  }
}
