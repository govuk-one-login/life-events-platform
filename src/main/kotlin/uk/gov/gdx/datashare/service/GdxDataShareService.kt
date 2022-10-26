package uk.gov.gdx.datashare.service

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.config.AuthenticationFacade
import uk.gov.gdx.datashare.repository.EventLookupRepository
import java.time.LocalDate

@Service
class GdxDataShareService(
  private val eventLookupRepository: EventLookupRepository,
  private val auditService: AuditService,
  private val levApiService: LevApiService,
  private val hmrcApiService: HmrcApiService,
  private val authenticationFacade: AuthenticationFacade,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun retrieveLevData(eventId: String): DwpData {
    log.info("Looking up event Id {} for DWP request", eventId)

    // Retrieve the ID from the lookup table
    val event = eventLookupRepository.findById(eventId) ?: throw RuntimeException("Not Found")

    // get the data from the LEV
    val citizenDeathId = event.levLookupId.toInt()
    val deathRecord = levApiService.findDeathById(citizenDeathId)

    val deathData = deathRecord.map {
      // go off and get more data here
      val nino = hmrcApiService.findNiNoByNameAndDob(
        surname = it.deceased.surname,
        firstname = it.deceased.forenames,
        dob = it.deceased.dateOfDeath
      )
      val niNumber = nino.awaitSingle().ni_number

      DwpData(id = it.id, date = it.date, deceased = it.deceased, partner = it.partner, niNumber = niNumber)
    }.first()

    // audit the event
    auditService.sendMessage(auditType = AuditType.CLIENT_CONSUMED_EVENT, id = eventId, details = deathData, username = authenticationFacade.getUsername())

    return deathData
  }
}

data class DwpData(
  val id: Long,
  val date: LocalDate,
  val deceased: Deceased,
  val partner: Partner?,
  val niNumber: String?,
)
