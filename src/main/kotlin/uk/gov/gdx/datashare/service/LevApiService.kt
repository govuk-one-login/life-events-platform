package uk.gov.gdx.datashare.service

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToFlow
import uk.gov.gdx.datashare.config.NoDataFoundException
import java.time.LocalDate

@Service
class LevApiService(
  private val levApiWebClient: WebClient,
  meterRegistry: MeterRegistry,
) {
  private val callsToLevCounter: Counter = meterRegistry.counter("API_CALLS.CallsToLev")
  private val responsesFromLevCounter: Counter = meterRegistry.counter("API_RESPONSES.ResponsesFromLev")

  fun findDeathById(id: Int): List<DeathRecord> {
    try {
      callsToLevCounter.increment()
      return runBlocking {
        val deathRecord = levApiWebClient.get()
          .uri("/v1/registration/death/$id")
          .retrieve()
          .bodyToFlow<DeathRecord>()
        responsesFromLevCounter.increment()
        deathRecord.toList()
      }
    } catch (e: WebClientResponseException) {
      throw if (e.statusCode.equals(HttpStatus.NOT_FOUND)) {
        NoDataFoundException(id.toString())
      } else {
        e
      }
    }
  }
}

data class DeathRecord(
  val id: String,
  val date: LocalDate,
  val deceased: Deceased,
  val status: DeathRecordStatus? = null,
)

data class Deceased(
  val forenames: String? = null,
  val surname: String,
  val dateOfDeath: LocalDate,
  val sex: Sex,
  val maidenSurname: String? = null,
  val birthplace: String? = null,
  val dateOfBirth: LocalDate? = null,
  val deathplace: String? = null,
  val occupation: String? = null,
  val retired: Boolean? = null,
  val address: String? = null,
)

data class DeathRecordStatus(
  val blocked: Boolean? = null,
)
