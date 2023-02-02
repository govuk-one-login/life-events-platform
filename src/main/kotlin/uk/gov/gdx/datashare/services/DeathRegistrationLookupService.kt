package uk.gov.gdx.datashare.services

import com.amazonaws.xray.spring.aop.XRayEnabled
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.models.DeathNotificationDetails
import uk.gov.gdx.datashare.models.LevDeathRecord

@Service
@XRayEnabled
class DeathRegistrationLookupService(
  private val levApiService: LevApiService,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getEnrichedPayload(
    dataId: String,
    enrichmentFields: List<EnrichmentField>,
  ): DeathNotificationDetails? {
    val citizenDeathId = dataId.toInt()
    return levApiService.findDeathById(citizenDeathId)
      .map {
        mapDeathNotificationDetails(it, enrichmentFields)
      }.first()
  }

  private fun mapDeathNotificationDetails(
    deathRecord: LevDeathRecord,
    ef: List<EnrichmentField>,
  ): DeathNotificationDetails {
    val deceased = deathRecord.deceased
    return DeathNotificationDetails(
      registrationDate = if (ef.contains(EnrichmentField.REGISTRATION_DATE)) deathRecord.date else null,
      firstNames = if (ef.contains(EnrichmentField.FIRST_NAMES)) deceased.forenames else null,
      lastName = if (ef.contains(EnrichmentField.LAST_NAME)) deceased.surname else null,
      sex = if (ef.contains(EnrichmentField.SEX)) deceased.sex else null,
      dateOfDeath = if (ef.contains(EnrichmentField.DATE_OF_DEATH)) deceased.dateOfDeath else null,
      dateOfBirth = if (ef.contains(EnrichmentField.DATE_OF_BIRTH)) deceased.dateOfBirth else null,
      birthPlace = if (ef.contains(EnrichmentField.BIRTH_PLACE)) deceased.birthplace else null,
      deathPlace = if (ef.contains(EnrichmentField.DEATH_PLACE)) deceased.deathplace else null,
      maidenName = if (ef.contains(EnrichmentField.MAIDEN_NAME)) deceased.maidenSurname else null,
      occupation = if (ef.contains(EnrichmentField.OCCUPATION)) deceased.occupation else null,
      retired = if (ef.contains(EnrichmentField.RETIRED)) deceased.retired else null,
      address = if (ef.contains(EnrichmentField.ADDRESS)) deceased.address else null,
    )
  }
}
