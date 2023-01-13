package uk.gov.gdx.datashare.service

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.flow.Flow
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

  suspend fun findDeathById(id: Int): Flow<DeathRecord> {
    try {
      callsToLevCounter.increment()
      val deathRecord = levApiWebClient.get()
        .uri("/v1/registration/death/$id")
        .retrieve()
        .bodyToFlow<DeathRecord>()
      responsesFromLevCounter.increment()
      return deathRecord
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
  val id: String?,
  val date: LocalDate?,
  val deceased: Deceased?,
  val partner: Partner?,
)

data class Partner(
  val name: String?,
  val occupation: String?,
  val retired: String?,
)

data class Deceased(
  val forenames: String?,
  val surname: String?,
  val dateOfBirth: LocalDate?,
  val dateOfDeath: LocalDate?,
  val dateOfDeathQualifier: String?,
  val sex: String?,
  val address: String?,
)
