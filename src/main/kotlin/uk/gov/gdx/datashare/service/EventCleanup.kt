package uk.gov.gdx.datashare.service

import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.repository.EventDataRepository
import java.time.LocalDateTime.now

@Service
class EventCleanup(
  private val eventDataRepository: EventDataRepository
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Scheduled(fixedRate = 60000 * 60)
  fun removeExpiredEvents() {

    runBlocking {
      val expiredTime = now()
      log.debug("Looking for events older than {}", expiredTime)
      eventDataRepository.deleteAllExpiredEvents(expiredTime)
    }
  }
}
