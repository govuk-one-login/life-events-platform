package uk.gov.gdx.datashare.services

import com.amazonaws.xray.spring.aop.XRayEnabled
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.gdx.datashare.config.NoDataFoundException
import uk.gov.gdx.datashare.models.PrisonerRecord

@Service
@XRayEnabled
class PrisonerApiService(
  private val prisonerSearchApiWebClient: WebClient,
  meterRegistry: MeterRegistry,
) {
  private val callsToPrisonerSearchCounter: Counter = meterRegistry.counter("API_CALLS.CallsToPrisonerSearch")
  private val responsesFromPrisonerSearchCounter: Counter =
    meterRegistry.counter("API_RESPONSES.ResponsesFromPrisonerSearch")
  private val errorsFromPrisonerSearchCounter: Counter =
    meterRegistry.counter("API_RESPONSES.ErrorsFromPrisonerSearch")

  fun findPrisonerById(id: String): PrisonerRecord? {
    return try {
      callsToPrisonerSearchCounter.increment()

      val prisonerRecord = prisonerSearchApiWebClient.get()
        .uri("/prisoner/$id")
        .retrieve()
        .bodyToMono(PrisonerRecord::class.java)
      responsesFromPrisonerSearchCounter.increment()
      prisonerRecord.block()
    } catch (e: WebClientResponseException) {
      errorsFromPrisonerSearchCounter.increment()
      throw if (e.statusCode.equals(HttpStatus.NOT_FOUND)) {
        NoDataFoundException(id)
      } else {
        e
      }
    }
  }
}
