package uk.gov.gdx.datashare.service

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.repository.EventLookupRepository
import java.time.LocalDate

@Service
class GdxDataShareService(
  private val eventLookupRepository: EventLookupRepository,
  private val auditService: AuditService,
  private val levApiService: LevApiService,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun retrieveLevData(eventId: String): DeathDataAndMore {
    log.info("Looking up event Id {} for DWP request", eventId)

    // Retrieve the ID from the lookup table
    val event = eventLookupRepository.findById(eventId) ?: throw RuntimeException("Not Found")

    // get the data from the LEV
    val deathRecord = levApiService.findDeathById(event.levLookupId.toInt())

    val deathData = deathRecord.map {
      // we could go off and get more data here
      DeathDataAndMore(it.id, it.date, "More Data obtained from other sources")
    }.first()

    // audit the event
    auditService.sendMessage(auditType = AuditType.CLIENT_CONSUMED_EVENT, id = eventId, details = deathData, username = "DWP")

    return deathData
  }
}

data class DeathDataAndMore(
  val id: Long,
  val date: LocalDate,
  val moreData: String,
)
