package uk.gov.gdx.datashare.service

import com.fasterxml.jackson.annotation.JsonInclude
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.config.AuthenticationFacade
import uk.gov.gdx.datashare.repository.DataConsumerRepository
import uk.gov.gdx.datashare.repository.EventDataRepository
import uk.gov.gdx.datashare.resource.DataResponseMessage
import java.time.LocalDate

@Service
class EventDataRetrievalService(
  private val eventDataRepository: EventDataRepository,
  private val auditService: AuditService,
  private val authenticationFacade: AuthenticationFacade,
  private val levApiService: LevApiService,
  private val hmrcApiService: HmrcApiService,
  private val dataConsumerRepository: DataConsumerRepository,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun retrieveData(eventId: String): DataResponseMessage {
    val oauthClient = authenticationFacade.getUsername()
    log.info("Looking up event Id {} for client request {}", eventId, oauthClient)

    // check if client is allowed to send
    val dataConsumer = dataConsumerRepository.findById(oauthClient)
      ?: throw RuntimeException("Client $oauthClient is not a known data consumer")

    // Retrieve the ID from the lookup table
    val event = eventDataRepository.findById(eventId) ?: throw RuntimeException("Event $eventId Not Found")

    // check if client is allowed to consume this type of event
    if (event.eventType !in dataConsumer.allowedEventTypes.split(",").toTypedArray()) {
      throw RuntimeException("Client ${dataConsumer.clientName} is not allowed to consume ${event.eventType} events")
    }

    auditService.sendMessage(
      auditType = AuditType.CLIENT_CONSUMED_EVENT,
      id = eventId,
      details = event.eventType,
      username = dataConsumer.clientName
    )

    return when (event.datasetType) {
      "DEATH_LEV" -> {
        // get the data from the LEV
        val citizenDeathId = event.dataId.toInt()
        levApiService.findDeathById(citizenDeathId)
          .map {
            val nino = if (dataConsumer.ninoRequired) getNino(
              it.deceased.surname,
              it.deceased.forenames,
              it.deceased.dateOfDeath
            ) else null

            DataResponseMessage(
              eventType = event.eventType,
              eventId = eventId,
              details = DeathNotification(
                deathDetails = DeathDetails(
                  forenames = it.deceased.forenames,
                  surname = it.deceased.surname,
                  dateOfBirth = it.deceased.dateOfBirth,
                  dateOfDeath = it.deceased.dateOfDeath,
                  sex = it.deceased.sex,
                  address = it.deceased.address
                ),
                additionalInformation = if (nino != null) AdditionalInformation(
                  nino = nino
                ) else null
              )
            )
          }.first()
      }

      "DEATH_CSV" -> {
        // get the data from the data store - it's a CSV file
        val csvLine = event.dataPayload!!.split(",").toTypedArray()
        val (surname, forenames, dateOfBirth, dateOfDeath, sex) = csvLine

        val nino = if (dataConsumer.ninoRequired) getNino(surname, forenames, LocalDate.parse(dateOfBirth)) else null

        DataResponseMessage(
          eventType = event.eventType,
          eventId = eventId,
          details = DeathNotification(
            deathDetails = DeathDetails(
              forenames = forenames,
              surname = surname,
              dateOfBirth = LocalDate.parse(dateOfBirth),
              dateOfDeath = LocalDate.parse(dateOfDeath),
              sex = sex
            ),
            additionalInformation = if (nino != null) AdditionalInformation(
              nino = nino
            ) else null
          )
        )
      }
      else -> {
        throw RuntimeException("Unknown DataSet ${event.datasetType}")
      }
    }
  }

  suspend fun getNino(
    surname: String,
    forenames: String,
    dateOfBirth: LocalDate
  ): String? =
    hmrcApiService.findNiNoByNameAndDob(
      surname = surname,
      firstname = forenames,
      dob = dateOfBirth
    ).map { nino -> nino.ni_number }
      .awaitSingle()
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DeathNotification(
  val deathDetails: DeathDetails?,
  val additionalInformation: AdditionalInformation? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DeathDetails(
  val forenames: String,
  val surname: String,
  val dateOfBirth: LocalDate,
  val dateOfDeath: LocalDate,
  val sex: String,
  val address: String? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AdditionalInformation(
  val nino: String? = null,
)
