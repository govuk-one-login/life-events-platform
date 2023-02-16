package uk.gov.gdx.datashare.services

import com.amazonaws.xray.spring.aop.XRayEnabled
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
@XRayEnabled
class LevApiService(
  private val levApiWebClient: WebClient,
) {
  fun findDeathById(id: Int): List<LevDeathRecord> {
    try {
      return runBlocking {
        val deathRecord = levApiWebClient.get()
          .uri("/v1/registration/death/{id}", id)
          .retrieve()
          .bodyToFlow<LevDeathRecord>()
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
