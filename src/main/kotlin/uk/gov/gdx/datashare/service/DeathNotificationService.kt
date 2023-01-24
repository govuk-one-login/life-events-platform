package uk.gov.gdx.datashare.service

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.media.Schema
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.config.UnknownDatasetException
import uk.gov.gdx.datashare.repository.*
import java.time.LocalDate

@Service
class DeathNotificationService(
  private val consumerSubscriptionRepository: ConsumerSubscriptionRepository,
  private val eventDataRepository: EventDataRepository,
  private val levApiService: LevApiService,
  private val objectMapper: ObjectMapper,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun mapDeathNotification(dataPayload: String): DeathNotificationDetails =
    objectMapper.readValue(dataPayload, DeathNotificationDetails::class.java)

  fun saveDeathNotificationEvents(
    details: DataDetail,
    dataProcessorMessage: DataProcessorMessage,
  ) {
    val consumerSubscriptions = consumerSubscriptionRepository.findAllByEventType(dataProcessorMessage.eventTypeId)

    val fullyEnrichedData = getAllEnrichedData(
      dataProcessorMessage.datasetId,
      details.id,
    )

    val eventData = consumerSubscriptions.map {
      val dataPayload =
        enrichEventPayload(
          it.enrichmentFields.split(",").toList(),
          fullyEnrichedData,
        )

      EventData(
        consumerSubscriptionId = it.id,
        datasetId = dataProcessorMessage.datasetId,
        dataId = details.id,
        dataPayload = dataPayload?.let { objectMapper.writeValueAsString(dataPayload) },
        eventTime = dataProcessorMessage.eventTime,
      )
    }.toList()

    eventDataRepository.saveAll(eventData).toList()
  }

  private fun enrichEventPayload(
    enrichmentFields: List<String>,
    fullyEnrichedData: DeathNotificationDetails?,
  ): DeathNotificationDetails? {
    return EnrichmentService.getDataWithOnlyFields(
      objectMapper,
      fullyEnrichedData,
      enrichmentFields,
    )
  }

  private fun getAllEnrichedData(
    datasetId: String,
    dataId: String,
  ): DeathNotificationDetails? = when (datasetId) {
    "DEATH_LEV" -> {
      // get the data from the LEV
      val citizenDeathId = dataId.toInt()
      levApiService.findDeathById(citizenDeathId)
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
    }

    "PASS_THROUGH" -> {
      null
    }

    else -> {
      throw UnknownDatasetException("Unknown DataSet $datasetId")
    }
  }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Death notification")
data class DeathNotificationDetails(
  @Schema(description = "Date the death was registered", required = true, example = "2022-01-05", type = "date")
  val registrationDate: LocalDate? = null,
  @Schema(description = "Forenames of the deceased", required = true, example = "Bob Burt")
  val firstNames: String? = null,
  @Schema(description = "Surname of the deceased", required = true, example = "Smith")
  val lastName: String? = null,
  @Schema(description = "Sex of the deceased", required = true, example = "Female", allowableValues = [ "Male", "Female", "Indeterminate"])
  val sex: String? = null,
  @Schema(description = "Date the person died", required = true, example = "2021-12-31", type = "date")
  val dateOfDeath: LocalDate? = null,
  @Schema(description = "Maiden name of the deceased", required = false, example = "Jane")
  val maidenName: String? = null,
  @Schema(description = "Date the deceased was born", required = false, example = "2001-12-31", type = "date")
  val dateOfBirth: LocalDate? = null,
  @Schema(description = "The deceased's address", required = false, example = "888 Death House, 8 Death lane, Deadington, Deadshire")
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
