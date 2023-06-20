package uk.gov.gdx.datashare.uk.gov.gdx.datashare.services

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.GroSex
import uk.gov.gdx.datashare.models.GroDeathRecord
import uk.gov.gdx.datashare.services.GroApiService
import uk.gov.gdx.datashare.services.GroDeathNotificationService
import java.time.LocalDate

class GroDeathNotificationServiceTest {
  private val groApiService = mockk<GroApiService>()

  private val underTest: GroDeathNotificationService = GroDeathNotificationService(groApiService)

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

  @Test
  fun `getEnrichedData returns all data for a full set of enrichment fields`() {
    val enrichmentFields = listOf(
      EnrichmentField.REGISTRATION_ID,
      EnrichmentField.EVENT_TIME,
      EnrichmentField.VERIFICATION_LEVEL,
      EnrichmentField.DATE_OF_DEATH,
      EnrichmentField.PARTIAL_MONTH_OF_DEATH,
      EnrichmentField.PARTIAL_YEAR_OF_DEATH,
      EnrichmentField.FORENAMES,
      EnrichmentField.SURNAME,
      EnrichmentField.MAIDEN_SURNAME,
      EnrichmentField.SEX,
      EnrichmentField.DATE_OF_BIRTH,
      EnrichmentField.ADDRESS_LINE_1,
      EnrichmentField.ADDRESS_LINE_2,
      EnrichmentField.ADDRESS_LINE_3,
      EnrichmentField.ADDRESS_LINE_4,
      EnrichmentField.POSTCODE,
    )

    every { groApiService.enrichEvent(dataId) } returns deathRecord

    val enrichedPayload = underTest.process(dataId, enrichmentFields)

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
  fun `getEnrichedData returns correct data for a subset of enrichment fields`() {
    val enrichmentFields = listOf(
      EnrichmentField.FORENAMES,
      EnrichmentField.DATE_OF_DEATH,
      EnrichmentField.ADDRESS_LINE_1,
      EnrichmentField.MAIDEN_SURNAME,
    )

    every { groApiService.enrichEvent(dataId) } returns deathRecord

    val enrichedPayload = underTest.process(dataId, enrichmentFields)

    assertThat(enrichedPayload.forenames).isEqualTo(deathRecord.forenames)
    assertThat(enrichedPayload.dateOfDeath).isEqualTo(deathRecord.dateOfDeath)
    assertThat(enrichedPayload.addressLine1).isEqualTo(deathRecord.addressLine1)
    assertThat(enrichedPayload.maidenSurname).isEqualTo(deathRecord.maidenSurname)
    assertThat(enrichedPayload.registrationId).isNull()
    assertThat(enrichedPayload.eventTime).isNull()
    assertThat(enrichedPayload.verificationLevel).isNull()
    assertThat(enrichedPayload.partialMonthOfDeath).isNull()
    assertThat(enrichedPayload.partialYearOfDeath).isNull()
    assertThat(enrichedPayload.surname).isNull()
    assertThat(enrichedPayload.sex).isNull()
    assertThat(enrichedPayload.dateOfBirth).isNull()
    assertThat(enrichedPayload.addressLine2).isNull()
    assertThat(enrichedPayload.addressLine3).isNull()
    assertThat(enrichedPayload.addressLine4).isNull()
    assertThat(enrichedPayload.postcode).isNull()
  }
}
