package uk.gov.gdx.datashare.service

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.media.Schema
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.enums.Sex
import uk.gov.gdx.datashare.models.DeathNotificationDetails
import java.time.LocalDate

@Service
class DeathNotificationService(
  private val levApiService: LevApiService,
  private val objectMapper: ObjectMapper,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getEnrichedPayload(
    dataId: String,
    enrichmentFields: List<String>,
  ): DeathNotificationDetails? {
    val citizenDeathId = dataId.toInt()
    val allEnrichedData = levApiService.findDeathById(citizenDeathId)
      .map {
        DeathNotificationDetails(
          registrationDate = it.date,
          firstNames = it.deceased.forenames,
          lastName = it.deceased.surname,
          sex = it.deceased.sex,
          dateOfDeath = it.deceased.dateOfDeath,
          dateOfBirth = it.deceased.dateOfBirth,
          birthPlace = it.deceased.birthplace,
          deathPlace = it.deceased.deathplace,
          maidenName = it.deceased.maidenSurname,
          occupation = it.deceased.occupation,
          retired = it.deceased.retired,
          address = it.deceased.address,
        )
      }.first()

    return EnrichmentService.getDataWithOnlyFields(objectMapper, allEnrichedData, enrichmentFields)
  }
}
