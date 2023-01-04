package uk.gov.gdx.datashare.service

import kotlinx.coroutines.runBlocking
import net.javacrumbs.shedlock.core.LockAssert
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.config.DateTimeHandler
import uk.gov.gdx.datashare.repository.EgressEventDataRepository
import uk.gov.gdx.datashare.repository.IngressEventDataRepository
import java.util.concurrent.TimeUnit

@Service
class EventCleanup(
  private val ingressEventDataRepository: IngressEventDataRepository,
  private val egressEventDataRepository: EgressEventDataRepository,
  private val dateTimeHandler: DateTimeHandler,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
  @SchedulerLock(name = "removeExpireEvents", lockAtMostFor = "3m", lockAtLeastFor = "3m")
  fun removeExpiredEvents() {
    runBlocking {
      LockAssert.assertLocked()
      val expiredTime = dateTimeHandler.now()
      log.debug("Looking for events older than {}", expiredTime)
      egressEventDataRepository.deleteAllExpiredEvents(expiredTime)
      ingressEventDataRepository.deleteAllOrphanedEvents()
    }
  }
}
