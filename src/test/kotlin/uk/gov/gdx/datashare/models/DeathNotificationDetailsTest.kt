package uk.gov.gdx.datashare.uk.gov.gdx.datashare.models

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.Sex
import uk.gov.gdx.datashare.models.DeathNotificationDetails
import uk.gov.gdx.datashare.models.LevDeathRecord
import uk.gov.gdx.datashare.models.LevDeceased
import java.time.LocalDate

class DeathNotificationDetailsTest {
  @Test
  fun `fromLevDeathRecord sets all values`() {
    val expectedRegistrationDate = LocalDate.of(2000, 12, 31)
    val expectedFirstNames = "forenames"
    val expectedLastName = "surname"
    val expectedSex = Sex.FEMALE
    val expectedDateOfDeath = LocalDate.of(2000, 12, 30)
    val expectedDateOfBirth = LocalDate.of(1900, 1, 3)
    val expectedBirthPlace = "birthplace"
    val expectedDeathPlace = "deathplace"
    val expectedMaidenName = "Maiden"
    val expectedOccupation = "occupation"
    val expectedRetired = true
    val expectedAddress = "address"
    val levDeathRecord = LevDeathRecord(
      id = "123",
      date = expectedRegistrationDate,
      deceased = LevDeceased(
        forenames = expectedFirstNames,
        surname = expectedLastName,
        dateOfDeath = expectedDateOfDeath,
        sex = expectedSex,
        maidenSurname = expectedMaidenName,
        birthplace = expectedBirthPlace,
        dateOfBirth = expectedDateOfBirth,
        deathplace = expectedDeathPlace,
        occupation = expectedOccupation,
        retired = expectedRetired,
        address = expectedAddress,
      ),
    )
    val enrichmentFields = listOf(
      EnrichmentField.REGISTRATION_DATE,
      EnrichmentField.FIRST_NAMES,
      EnrichmentField.LAST_NAME,
      EnrichmentField.SEX,
      EnrichmentField.DATE_OF_DEATH,
      EnrichmentField.DATE_OF_BIRTH,
      EnrichmentField.BIRTH_PLACE,
      EnrichmentField.DEATH_PLACE,
      EnrichmentField.MAIDEN_NAME,
      EnrichmentField.OCCUPATION,
      EnrichmentField.RETIRED,
      EnrichmentField.ADDRESS,
    )

    val deathNotificationDetails = DeathNotificationDetails.fromLevDeathRecord(enrichmentFields, levDeathRecord)

    with(deathNotificationDetails) {
      assertThat(registrationDate).isEqualTo(expectedRegistrationDate)
      assertThat(firstNames).isEqualTo(expectedFirstNames)
      assertThat(lastName).isEqualTo(expectedLastName)
      assertThat(sex).isEqualTo(expectedSex)
      assertThat(dateOfDeath).isEqualTo(expectedDateOfDeath)
      assertThat(dateOfBirth).isEqualTo(expectedDateOfBirth)
      assertThat(birthPlace).isEqualTo(expectedBirthPlace)
      assertThat(deathPlace).isEqualTo(expectedDeathPlace)
      assertThat(maidenName).isEqualTo(expectedMaidenName)
      assertThat(occupation).isEqualTo(expectedOccupation)
      assertThat(retired).isEqualTo(expectedRetired)
      assertThat(address).isEqualTo(expectedAddress)
    }
  }

  @Test
  fun `fromLevDeathRecord only sets allowed values`() {
    val levDeathRecord = LevDeathRecord(
      id = "123",
      date = LocalDate.of(2000, 12, 31),
      deceased = LevDeceased(
        forenames = "forenames",
        surname = "surname",
        dateOfDeath = LocalDate.of(2000, 12, 30),
        sex = Sex.FEMALE,
        maidenSurname = "Maiden",
        birthplace = "birthplace",
        dateOfBirth = LocalDate.of(1900, 1, 3),
        deathplace = "deathplace",
        occupation = "occupation",
        retired = true,
        address = "address",
      ),
    )
    val enrichmentFieldNames = emptyList<EnrichmentField>()

    val deathNotificationDetails = DeathNotificationDetails.fromLevDeathRecord(enrichmentFieldNames, levDeathRecord)

    with(deathNotificationDetails) {
      assertThat(registrationDate).isNull()
      assertThat(firstNames).isNull()
      assertThat(lastName).isNull()
      assertThat(sex).isNull()
      assertThat(dateOfDeath).isNull()
      assertThat(dateOfBirth).isNull()
      assertThat(birthPlace).isNull()
      assertThat(deathPlace).isNull()
      assertThat(maidenName).isNull()
      assertThat(occupation).isNull()
      assertThat(retired).isNull()
      assertThat(address).isNull()
    }
  }

  @Test
  fun `allowed fields are included in serialized result`() {
    val deathNotificationDetails = DeathNotificationDetails(listOf(EnrichmentField.FIRST_NAMES))
    val ser = ObjectMapper().writeValueAsString(deathNotificationDetails)
    assertThat(ser).isEqualTo("{\"firstNames\":null}")
  }
}
