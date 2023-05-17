package uk.gov.gdx.datashare.uk.gov.gdx.datashare.services

import io.micrometer.core.instrument.MeterRegistry
import io.mockk.*
import net.javacrumbs.shedlock.core.LockAssert
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.helpers.TimeLimitedRepeater
import uk.gov.gdx.datashare.repositories.AcquirerEventRepository
import uk.gov.gdx.datashare.repositories.SupplierEvent
import uk.gov.gdx.datashare.repositories.SupplierEventRepository
import uk.gov.gdx.datashare.services.GroApiService
import uk.gov.gdx.datashare.services.ScheduledJobService
import java.util.*

class ScheduledJobServiceTest {
  private val acquirerEventRepository = mockk<AcquirerEventRepository>()
  private val groApiService = mockk<GroApiService>()
  private val supplierEventRepository = mockk<SupplierEventRepository>()
  private val meterRegistry = mockk<MeterRegistry>()

  private val underTest: ScheduledJobService = ScheduledJobService(
    acquirerEventRepository,
    groApiService,
    supplierEventRepository,
    meterRegistry,
  )

  @Test
  fun `deleteConsumedGroSupplierEvents deletes multiple events`() {
    mockkStatic(LockAssert::class)
    every { LockAssert.assertLocked() } just runs
    mockkStatic(TimeLimitedRepeater::class)
    every { TimeLimitedRepeater.repeat(any<List<SupplierEvent>>(), any()) } just runs

    val supplierEvent1 = SupplierEvent(
      supplierSubscriptionId = UUID.randomUUID(),
      dataId = "asdasd",
      eventTime = null,
    )
    val supplierEvent2 = SupplierEvent(
      supplierSubscriptionId = UUID.randomUUID(),
      dataId = "asdasd2",
      eventTime = null,
    )
    val supplierEvents = listOf(supplierEvent1, supplierEvent2)
    every { supplierEventRepository.findGroDeathEventsForDeletion() } returns supplierEvents

    underTest.deleteConsumedGroSupplierEvents()

    verify(exactly = 1) {
      TimeLimitedRepeater.repeat(
        withArg<List<SupplierEvent>> {
          assertThat(it).isEqualTo(supplierEvents)
        },
        any(),
      )
    }
  }
}
