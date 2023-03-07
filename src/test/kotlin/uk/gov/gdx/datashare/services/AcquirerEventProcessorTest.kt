package uk.gov.gdx.datashare.uk.gov.gdx.datashare.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import uk.gov.gdx.datashare.repositories.AcquirerEvent
import uk.gov.gdx.datashare.repositories.AcquirerEventRepository
import uk.gov.gdx.datashare.repositories.AcquirerSubscriptionRepository
import uk.gov.gdx.datashare.services.AcquirerEventProcessor
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.helpers.builders.AcquirerEventBuilder
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.helpers.builders.AcquirerSubscriptionBuilder
import java.util.*

class AcquirerEventProcessorTest {
  private val objectMapper = mockk<ObjectMapper>()
  private val acquirerSubscriptionRepository = mockk<AcquirerSubscriptionRepository>()
  private val acquirerEventRepository = mockk<AcquirerEventRepository>()

  @Test
  fun `saves events to the database`() {
    val acquirerSubscription = AcquirerSubscriptionBuilder().also {
      it.queueName = null
    }.build()
    val acquirerEvent = AcquirerEventBuilder().also {
      it.acquirerSubscriptionId = acquirerSubscription.id
    }.build()
    every { acquirerSubscriptionRepository.findByIdOrNull(any<UUID>()) }.returns(acquirerSubscription)
    every { objectMapper.readValue(any<String>(), AcquirerEvent::class.java) }.returns(acquirerEvent)
    every { acquirerEventRepository.save(any<AcquirerEvent>()) }.returns(acquirerEvent)

    val underTest = AcquirerEventProcessor(
      objectMapper,
      acquirerEventRepository,
    )

    underTest.onAcquirerEvent("")

    verify(exactly = 1) {
      acquirerEventRepository.save(
        withArg<AcquirerEvent> {
          assertThat(it.id).isEqualTo(acquirerEvent.id)
          assertNull(it.deletedAt)
          assertThat(it.supplierEventId).isEqualTo(acquirerEvent.supplierEventId)
          assertThat(it.acquirerSubscriptionId).isEqualTo(acquirerEvent.acquirerSubscriptionId)
          assertThat(it.dataId).isEqualTo(acquirerEvent.dataId)
          assertThat(it.eventTime).isEqualTo(acquirerEvent.eventTime)
          assertThat(it.createdAt).isEqualTo(acquirerEvent.createdAt)
        },
      )
    }
  }
}
