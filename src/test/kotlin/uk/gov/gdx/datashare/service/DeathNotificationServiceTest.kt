package uk.gov.gdx.datashare.service

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.config.JacksonConfiguration
import java.time.LocalDate

class DeathNotificationServiceTest {
  private val levApiService = mockk<LevApiService>()
  private val objectMapper = JacksonConfiguration().objectMapper()

  private val underTest: DeathNotificationService = DeathNotificationService(
    levApiService,
    objectMapper,
  )

  @Test
  fun `getEnrichedData returns all data for a full set of enrichment fields`() {
    val dataId = "123456789"
    val enrichmentFields = listOf(
      "registrationDate",
      "firstNames",
      "lastName",
      "sex",
      "dateOfDeath",
      "maidenName",
      "dateOfBirth",
      "address",
      "birthPlace",
      "deathPlace",
      "occupation",
      "retired",
    )
    val deathRecord = DeathRecord(
      deceased = Deceased(
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
    val enrichmentFields = listOf("firstNames", "dateOfDeath", "address", "retired")
    val deathRecord = DeathRecord(
      deceased = Deceased(
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
