package uk.gov.gdx.datashare.service

import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.repository.EgressEventDataRepository
import uk.gov.gdx.datashare.repository.IngressEventDataRepository
import java.time.LocalDateTime.now
import java.util.concurrent.TimeUnit

@Service
class EventCleanup(
  private val ingressEventDataRepository: IngressEventDataRepository,
  private val egressEventDataRepository: EgressEventDataRepository
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
  fun removeExpiredEvents() {
    runBlocking {
      val expiredTime = now()
      log.debug("Looking for events older than {}", expiredTime)
      egressEventDataRepository.deleteAllExpiredEvents(expiredTime)
      ingressEventDataRepository.deleteAllOrphanedEvents()
    }
  }
}
