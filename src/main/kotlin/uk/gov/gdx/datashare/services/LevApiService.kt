package uk.gov.gdx.datashare.services

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
import uk.gov.gdx.datashare.models.LevDeathRecord

@Service
class LevApiService(
  private val levApiWebClient: WebClient,
  meterRegistry: MeterRegistry,
) {
  private val callsToLevCounter: Counter = meterRegistry.counter("API_CALLS.CallsToLev")
  private val responsesFromLevCounter: Counter = meterRegistry.counter("API_RESPONSES.ResponsesFromLev")

  fun findDeathById(id: Int): List<LevDeathRecord> {
    try {
      callsToLevCounter.increment()
      return runBlocking {
        val deathRecord = levApiWebClient.get()
          .uri("/v1/registration/death/$id")
          .retrieve()
          .bodyToFlow<LevDeathRecord>()
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
