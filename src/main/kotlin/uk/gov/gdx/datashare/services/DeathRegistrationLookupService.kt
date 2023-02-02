package uk.gov.gdx.datashare.services

import com.amazonaws.xray.spring.aop.XRayEnabled
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.enums.DeathNotificationField
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
    enrichmentFields: List<DeathNotificationField>,
  ): DeathNotificationDetails? {
    val citizenDeathId = dataId.toInt()
    return levApiService.findDeathById(citizenDeathId)
      .map {
        mapDeathNotificationDetails(it, enrichmentFields)
      }.first()
  }

  private fun mapDeathNotificationDetails(
    deathRecord: LevDeathRecord,
    ef: List<DeathNotificationField>,
  ): DeathNotificationDetails {
    val deceased = deathRecord.deceased
    return DeathNotificationDetails(
      registrationDate = if (ef.contains(DeathNotificationField.REGISTRATION_DATE)) deathRecord.date else null,
      firstNames = if (ef.contains(DeathNotificationField.FIRST_NAMES)) deceased.forenames else null,
      lastName = if (ef.contains(DeathNotificationField.LAST_NAME)) deceased.surname else null,
      sex = if (ef.contains(DeathNotificationField.SEX)) deceased.sex else null,
      dateOfDeath = if (ef.contains(DeathNotificationField.DATE_OF_DEATH)) deceased.dateOfDeath else null,
      dateOfBirth = if (ef.contains(DeathNotificationField.DATE_OF_BIRTH)) deceased.dateOfBirth else null,
      birthPlace = if (ef.contains(DeathNotificationField.BIRTH_PLACE)) deceased.birthplace else null,
      deathPlace = if (ef.contains(DeathNotificationField.DEATH_PLACE)) deceased.deathplace else null,
      maidenName = if (ef.contains(DeathNotificationField.MAIDEN_NAME)) deceased.maidenSurname else null,
      occupation = if (ef.contains(DeathNotificationField.OCCUPATION)) deceased.occupation else null,
      retired = if (ef.contains(DeathNotificationField.RETIRED)) deceased.retired else null,
      address = if (ef.contains(DeathNotificationField.ADDRESS)) deceased.address else null,
    )
  }
}
