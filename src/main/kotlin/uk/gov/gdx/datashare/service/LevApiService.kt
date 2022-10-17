package uk.gov.gdx.datashare.service

import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlow
import java.time.LocalDate

@Service
class LevApiService(
  private val levApiWebClient: WebClient
) {

  suspend fun findDeathById(id: Int): Flow<DeathRecord> =
    levApiWebClient.get()
      .uri("/v1/registration/death/$id")
      .retrieve()
      .bodyToFlow()
}

data class DeathRecord(
  val id: Long,
  val date: LocalDate,
  val deceased: Deceased,
  val partner: Partner?,
)
data class Partner(
  val name: String?,
  val occupation: String?,
  val retired: String?
)
data class Deceased(
  val forenames: String,
  val surname: String,
  val dateOfBirth: LocalDate,
  val dateOfDeath: LocalDate,
  val dateOfDeathQualifier: String?,
  val sex: String,
  val address: String?,
)
