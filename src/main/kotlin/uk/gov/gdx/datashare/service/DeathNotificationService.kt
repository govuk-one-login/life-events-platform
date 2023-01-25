package uk.gov.gdx.datashare.service

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.media.Schema
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.stereotype.Service
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
    datasetId: String,
    enrichmentFields: List<String>,
  ): DeathNotificationDetails? = when (datasetId) {
    "DEATH_LEV" -> {
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

      EnrichmentService.getDataWithOnlyFields(objectMapper, allEnrichedData, enrichmentFields)
    }

    else -> {
      null
    }
  }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Death notification")
data class DeathNotificationDetails(

  @Schema(description = "Date the death was registered", required = true, example = "2022-01-05", type = "date")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  val registrationDate: LocalDate? = null,

  @Schema(description = "Forenames of the deceased", required = true, example = "Bob Burt")
  val firstNames: String? = null,

  @Schema(description = "Surname of the deceased", required = true, example = "Smith")
  val lastName: String? = null,

  @Schema(description = "Sex of the deceased", required = true, example = "Female")
  val sex: Sex? = null,

  @Schema(description = "Date the person died", required = true, example = "2021-12-31", type = "date")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  val dateOfDeath: LocalDate? = null,

  @Schema(description = "Maiden name of the deceased", required = false, example = "Jane")
  val maidenName: String? = null,

  @Schema(description = "Date the deceased was born", required = false, example = "2001-12-31", type = "date")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  val dateOfBirth: LocalDate? = null,

  @Schema(
    description = "The deceased's address",
    required = false,
    example = "888 Death House, 8 Death lane, Deadington, Deadshire",
  )
  val address: String? = null,

  @Schema(description = "The birthplace of the deceased", required = false, example = "Sheffield")
  val birthPlace: String? = null,

  @Schema(description = "The place the person died", required = false, example = "Swansea")
  val deathPlace: String? = null,

  @Schema(description = "The occupation of the deceased", required = false, example = "Doctor")
  val occupation: String? = null,

  @Schema(description = "Whether the deceased was retired", required = false, example = "false")
  val retired: Boolean? = null,
)

enum class Sex(@JsonValue val jsonName: String) {
  MALE("Male"),
  FEMALE("Female"),
  INDETERMINATE("Indeterminate"),
}
