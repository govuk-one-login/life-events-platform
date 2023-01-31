package uk.gov.gdx.datashare.services

import com.amazonaws.xray.spring.aop.XRayEnabled
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.enums.Sex
import uk.gov.gdx.datashare.models.PrisonerDetails

@Service
@XRayEnabled
class PrisonerLookupService(
  private val prisonerApiService: PrisonerApiService,
  private val objectMapper: ObjectMapper,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getEnrichedPayload(
    prisonerNumber: String,
    enrichmentFields: List<String>,
  ): PrisonerDetails? {
    val allEnrichedData = prisonerApiService.findPrisonerById(prisonerNumber)
      ?.let {
        PrisonerDetails(
          prisonerNumber = prisonerNumber,
          firstName = it.firstName,
          middleNames = it.middleNames,
          lastName = it.lastName,
          sex = when (it.gender) { "Female" -> Sex.FEMALE "Male" -> Sex.MALE else -> Sex.INDETERMINATE },
          dateOfBirth = it.dateOfBirth,
        )
      }

    return EnrichmentService.getDataWithOnlyFields(objectMapper, allEnrichedData, enrichmentFields)
  }
}
