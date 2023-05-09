package uk.gov.gdx.datashare.services

import com.amazonaws.xray.spring.aop.XRayEnabled
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.enums.GroSex
import uk.gov.gdx.datashare.models.GroDeathNotificationDetails
import uk.gov.gdx.datashare.models.GroDeathRecord
import java.time.LocalDate

@Service
@XRayEnabled
class GroDeathNotificationService : EnrichmentService {
  override fun accepts(eventType: EventType): Boolean {
    return eventType == EventType.GRO_DEATH_NOTIFICATION
  }

  override fun process(
    eventType: EventType,
    dataId: String,
    enrichmentFields: List<EnrichmentField>,
  ): GroDeathNotificationDetails {
    val temporaryLevDeathRecord = GroDeathRecord(
      hash = dataId,
      registrationId = "registrationId",
      eventTime = LocalDate.now(),
      verificationLevel = "1",
      dateOfDeath = LocalDate.now(),
      partialMonthOfDeath = "2",
      partialYearOfDeath = "2020",
      forenames = "forenames",
      surname = "surname",
      maidenSurname = "maidenSurname",
      sex = GroSex.FEMALE,
      dateOfBirth = LocalDate.now(),
      addressLine1 = "addressLine1",
      addressLine2 = "addressLine2",
      addressLine3 = "addressLine3",
      addressLine4 = "addressLine4",
      postcode = "postcode",
    )
    return GroDeathNotificationDetails.from(enrichmentFields, temporaryLevDeathRecord)
  }
}
