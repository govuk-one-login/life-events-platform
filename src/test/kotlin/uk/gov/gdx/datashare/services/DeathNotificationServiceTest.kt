package uk.gov.gdx.datashare.services

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.enums.DeathNotificationField
import uk.gov.gdx.datashare.enums.Sex
import uk.gov.gdx.datashare.models.LevDeathRecord
import uk.gov.gdx.datashare.models.LevDeceased
import java.time.LocalDate

class DeathNotificationServiceTest {
  private val levApiService = mockk<LevApiService>()

  private val underTest: DeathNotificationService = DeathNotificationService(
    levApiService,
  )

  @Test
  fun `getEnrichedData returns all data for a full set of enrichment fields`() {
    val dataId = "123456789"
    val enrichmentFields = listOf(
      DeathNotificationField.REGISTRATION_DATE,
      DeathNotificationField.FIRST_NAMES,
      DeathNotificationField.LAST_NAME,
      DeathNotificationField.SEX,
      DeathNotificationField.DATE_OF_DEATH,
      DeathNotificationField.MAIDEN_NAME,
      DeathNotificationField.DATE_OF_BIRTH,
      DeathNotificationField.ADDRESS,
      DeathNotificationField.BIRTH_PLACE,
      DeathNotificationField.DEATH_PLACE,
      DeathNotificationField.OCCUPATION,
      DeathNotificationField.RETIRED,
    )
    val deathRecord = LevDeathRecord(
      deceased = LevDeceased(
        forenames = "Alice",
        surname = "Smith",
        dateOfBirth = LocalDate.of(1920, 1, 1),
        dateOfDeath = LocalDate.of(2010, 1, 1),
        sex = Sex.FEMALE,
        address = "666 Inform House, 6 Inform street, Informington, Informshire",
        birthplace = "Hospital",
        deathplace = "Home",
        maidenSurname = "Jones",
        occupation = "Doctor",
        retired = true,
      ),
      id = dataId,
      date = LocalDate.now(),
    )

    every { levApiService.findDeathById(dataId.toInt()) }.returns(listOf(deathRecord))

    val enrichedPayload = underTest.getEnrichedPayload(dataId, enrichmentFields)!!

    assertThat(enrichedPayload.firstNames).isEqualTo(deathRecord.deceased.forenames)
    assertThat(enrichedPayload.lastName).isEqualTo(deathRecord.deceased.surname)
    assertThat(enrichedPayload.dateOfBirth).isEqualTo(deathRecord.deceased.dateOfBirth)
    assertThat(enrichedPayload.dateOfDeath).isEqualTo(deathRecord.deceased.dateOfDeath)
    assertThat(enrichedPayload.address).isEqualTo(deathRecord.deceased.address)
    assertThat(enrichedPayload.registrationDate).isEqualTo(deathRecord.date)
    assertThat(enrichedPayload.birthPlace).isEqualTo(deathRecord.deceased.birthplace)
    assertThat(enrichedPayload.deathPlace).isEqualTo(deathRecord.deceased.deathplace)
    assertThat(enrichedPayload.maidenName).isEqualTo(deathRecord.deceased.maidenSurname)
    assertThat(enrichedPayload.retired).isEqualTo(deathRecord.deceased.retired)
  }

  @Test
  fun `getEnrichedData returns correct data for a subset of enrichment fields`() {
    val dataId = "123456789"
    val enrichmentFields = listOf(
      DeathNotificationField.FIRST_NAMES,
      DeathNotificationField.DATE_OF_DEATH,
      DeathNotificationField.ADDRESS,
      DeathNotificationField.RETIRED,
    )
    val deathRecord = LevDeathRecord(
      deceased = LevDeceased(
        forenames = "Alice",
        surname = "Smith",
        dateOfBirth = LocalDate.of(1920, 1, 1),
        dateOfDeath = LocalDate.of(2010, 1, 1),
        sex = Sex.FEMALE,
        address = "666 Inform House, 6 Inform street, Informington, Informshire",
        birthplace = "Hospital",
        deathplace = "Home",
        maidenSurname = "Jones",
        occupation = "Doctor",
        retired = true,
      ),
      id = dataId,
      date = LocalDate.now(),
    )

    every { levApiService.findDeathById(dataId.toInt()) }.returns(listOf(deathRecord))

    val enrichedPayload = underTest.getEnrichedPayload(dataId, enrichmentFields)!!

    assertThat(enrichedPayload.firstNames).isEqualTo(deathRecord.deceased.forenames)
    assertThat(enrichedPayload.lastName).isNull()
    assertThat(enrichedPayload.dateOfBirth).isNull()
    assertThat(enrichedPayload.dateOfDeath).isEqualTo(deathRecord.deceased.dateOfDeath)
    assertThat(enrichedPayload.address).isEqualTo(deathRecord.deceased.address)
    assertThat(enrichedPayload.sex).isNull()
    assertThat(enrichedPayload.registrationDate).isNull()
    assertThat(enrichedPayload.birthPlace).isNull()
    assertThat(enrichedPayload.deathPlace).isNull()
    assertThat(enrichedPayload.maidenName).isNull()
    assertThat(enrichedPayload.retired).isEqualTo(deathRecord.deceased.retired)
  }
}
