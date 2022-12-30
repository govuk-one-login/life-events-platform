package uk.gov.gdx.datashare.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.repository.*
import java.time.LocalDate

class DeathNotificationServiceTest {
  private val consumerSubscriptionRepository = mockk<ConsumerSubscriptionRepository>()
  private val egressEventDataRepository = mockk<EgressEventDataRepository>()
  private val eventPublishingService = mockk<EventPublishingService>()
  private val levApiService = mockk<LevApiService>()

  private val underTest = DeathNotificationService(
    consumerSubscriptionRepository,
    egressEventDataRepository,
    eventPublishingService,
    levApiService,
    ObjectMapper()
  )

  @Test
  fun `mapDeathNotification maps string to full DeathNotificationDetails`() {
    val input =
      "{\"firstName\":\"Alice\",\"lastName\":\"Smith\",\"dateOfBirth\":\"1910-01-01\",\"dateOfDeath\":\"2010-12-12\",\"address\":\"666 Inform House, 6 Inform street, Informington, Informshire\",\"sex\":\"female\"}"

    val deathNotificationDetails = underTest.mapDeathNotification(input)

    assertThat(deathNotificationDetails).isEqualTo(
      DeathNotificationDetails(
        firstName = "Alice",
        lastName = "Smith",
        dateOfBirth = LocalDate.of(1910, 1, 1),
        dateOfDeath = LocalDate.of(2010, 12, 12),
        address = "666 Inform House, 6 Inform street, Informington, Informshire",
        sex = "female"
      )
    )
  }

  @Test
  fun `mapDeathNotification maps string to empty DeathNotificationDetails`() {
    val input = "{}"

    val deathNotificationDetails = underTest.mapDeathNotification(input)

    assertThat(deathNotificationDetails).isEqualTo(DeathNotificationDetails())
  }
}
