package uk.gov.gdx.datashare.services

import com.amazonaws.xray.spring.aop.XRayEnabled
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.enums.Gender
import uk.gov.gdx.datashare.models.PrisonerDetails
import uk.gov.gdx.datashare.models.PrisonerRecord

@Service
@XRayEnabled
class PrisonerLookupService(
  private val prisonerApiService: PrisonerApiService,
) : EnrichmentService {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  override fun accepts(eventType: EventType): Boolean {
    return eventType == EventType.ENTERED_PRISON
  }

  override fun process(
    eventType: EventType,
    dataId: String,
    enrichmentFields: List<EnrichmentField>,
  ): PrisonerDetails? {
    val allEnrichedData = prisonerApiService.findPrisonerById(dataId)

    return allEnrichedData?.let { mapPrisonerRecord(it, enrichmentFields) }
  }

  private fun mapPrisonerRecord(
    prisonerRecord: PrisonerRecord,
    ef: List<EnrichmentField>,
  ): PrisonerDetails {
    val gender = when (prisonerRecord.gender) {
      "Female" -> Gender.FEMALE
      "Male" -> Gender.MALE
      else -> Gender.INDETERMINATE
    }
    return PrisonerDetails(
      prisonerNumber = if (ef.contains(EnrichmentField.PRISONER_NUMBER)) prisonerRecord.prisonerNumber else null,
      firstName = if (ef.contains(EnrichmentField.FIRST_NAME)) prisonerRecord.firstName else null,
      middleNames = if (ef.contains(EnrichmentField.MIDDLE_NAMES)) prisonerRecord.middleNames else null,
      lastName = if (ef.contains(EnrichmentField.LAST_NAME)) prisonerRecord.lastName else null,
      gender = if (ef.contains(EnrichmentField.GENDER)) gender else null,
      dateOfBirth = if (ef.contains(EnrichmentField.DATE_OF_BIRTH)) prisonerRecord.dateOfBirth else null,
      prisonName = if (ef.contains(EnrichmentField.PRISON_NAME)) prisonerRecord.prisonName else null,
    )
  }
}
