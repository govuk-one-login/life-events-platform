package uk.gov.gdx.datashare.services

import com.amazonaws.xray.spring.aop.XRayEnabled
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
) {
  fun findPrisonerById(id: String): PrisonerRecord? {
    try {
      val prisonerRecord = prisonerSearchApiWebClient.get()
        .uri("/prisoner/{id}", id)
        .retrieve()
        .bodyToMono(PrisonerRecord::class.java)
      return prisonerRecord.block()
    } catch (e: WebClientResponseException) {
      throw if (e.statusCode.equals(HttpStatus.NOT_FOUND)) {
        NoDataFoundException(id)
      } else {
        e
      }
    }
  }
}
