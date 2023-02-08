package uk.gov.gdx.datashare.uk.gov.gdx.datashare.services

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.enums.Sex
import uk.gov.gdx.datashare.models.PrisonerRecord
import uk.gov.gdx.datashare.services.PrisonerApiService
import uk.gov.gdx.datashare.services.PrisonerLookupService
import java.time.LocalDate

class PrisonerLookupServiceTest {
  private val prisonerApiService = mockk<PrisonerApiService>()

  private val underTest: PrisonerLookupService = PrisonerLookupService(
    prisonerApiService,
  )

  @Test
  fun `getEnrichedData returns all data for a full set of enrichment fields`() {
    val dataId = "A1234AB"
    val enrichmentFields = listOf(
      EnrichmentField.FIRST_NAME,
      EnrichmentField.LAST_NAME,
      EnrichmentField.SEX,
      EnrichmentField.PRISONER_NUMBER,
      EnrichmentField.MIDDLE_NAMES,
      EnrichmentField.DATE_OF_BIRTH,
    )
    val prisonerRecord = PrisonerRecord(
      prisonerNumber = dataId,
      firstName = "Test",
      middleNames = "MiddleNames",
      lastName = "TestLastName",
      gender = "Male",
      dateOfBirth = LocalDate.of(1974, 3, 4),
    )

    every { prisonerApiService.findPrisonerById(dataId) }.returns(prisonerRecord)

    val enrichedPayload = underTest.process(EventType.ENTERED_PRISON, dataId, enrichmentFields)!!

    assertThat(enrichedPayload.firstName).isEqualTo(prisonerRecord.firstName)
    assertThat(enrichedPayload.lastName).isEqualTo(prisonerRecord.lastName)
    assertThat(enrichedPayload.dateOfBirth).isEqualTo(prisonerRecord.dateOfBirth)
    assertThat(enrichedPayload.middleNames).isEqualTo(prisonerRecord.middleNames)
    assertThat(enrichedPayload.prisonerNumber).isEqualTo(prisonerRecord.prisonerNumber)
    assertThat(enrichedPayload.sex).isEqualTo(Sex.MALE)
  }

  @Test
  fun `getEnrichedData returns correct data for a subset of enrichment fields`() {
    val dataId = "A1234AC"
    val enrichmentFields = listOf(
      EnrichmentField.FIRST_NAME,
      EnrichmentField.SEX,
      EnrichmentField.DATE_OF_BIRTH,
    )
    val prisonerRecord = PrisonerRecord(
      prisonerNumber = dataId,
      firstName = "Test",
      middleNames = "MiddleNames",
      lastName = "TestLastName",
      gender = "Non-Binary",
      dateOfBirth = LocalDate.of(1974, 3, 4),
    )

    every { prisonerApiService.findPrisonerById(dataId) }.returns(prisonerRecord)

    val enrichedPayload = underTest.process(EventType.ENTERED_PRISON, dataId, enrichmentFields)!!

    assertThat(enrichedPayload.firstName).isEqualTo(prisonerRecord.firstName)
    assertThat(enrichedPayload.lastName).isNull()
    assertThat(enrichedPayload.dateOfBirth).isEqualTo(prisonerRecord.dateOfBirth)
    assertThat(enrichedPayload.middleNames).isNull()
    assertThat(enrichedPayload.prisonerNumber).isNull()
    assertThat(enrichedPayload.sex).isEqualTo(Sex.INDETERMINATE)
  }
}
