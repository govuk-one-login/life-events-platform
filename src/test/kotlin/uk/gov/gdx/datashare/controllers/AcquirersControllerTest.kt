package uk.gov.gdx.datashare.controllers

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.enums.DeathNotificationField
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.models.AcquirerRequest
import uk.gov.gdx.datashare.models.AcquirerSubRequest
import uk.gov.gdx.datashare.models.AcquirerSubscriptionDto
import uk.gov.gdx.datashare.repositories.Acquirer
import uk.gov.gdx.datashare.services.AcquirersService
import java.time.LocalDateTime
import java.util.*

class AcquirersControllerTest {
  private val acquirersService = mockk<AcquirersService>()

  private val underTest = AcquirersController(acquirersService)

  @Test
  fun `getAcquirers gets acquirers`() {
    val acquirers = listOf(
      Acquirer(
        name = "Acquirer 1",
      ),
      Acquirer(
        name = "Acquirer 2",
      ),
    )

    every { acquirersService.getAcquirers() }.returns(acquirers)

    val acquirersOutput = underTest.getAcquirers().toList()

    assertThat(acquirersOutput).hasSize(2)
    assertThat(acquirersOutput).isEqualTo(acquirers.toList())
  }

  @Test
  fun `addAcquirer adds acquirer`() {
    val acquirerRequest = AcquirerRequest(
      name = "Acquirer",
    )
    val acquirer = Acquirer(name = acquirerRequest.name)

    every { acquirersService.addAcquirer(any()) }.returns(acquirer)

    val acquirerOutput = underTest.addAcquirer(acquirerRequest)

    assertThat(acquirerOutput).isEqualTo(acquirer)
  }

  @Test
  fun `getAcquirerSubscriptions gets acquirer subscriptions`() {
    val acquirerSubscriptionDtos = listOf(
      AcquirerSubscriptionDto(
        acquirerSubscriptionId = UUID.randomUUID(),
        acquirerId = UUID.randomUUID(),
        eventType = EventType.DEATH_NOTIFICATION,
        enrichmentFields = listOf(DeathNotificationField.FIRST_NAMES),
        enrichmentFieldsIncludedInPoll = false,
        whenCreated = LocalDateTime.now(),
      ),
      AcquirerSubscriptionDto(
        acquirerSubscriptionId = UUID.randomUUID(),
        acquirerId = UUID.randomUUID(),
        eventType = EventType.DEATH_NOTIFICATION,
        enrichmentFields = listOf(DeathNotificationField.FIRST_NAMES),
        enrichmentFieldsIncludedInPoll = false,
        whenCreated = LocalDateTime.now(),
      ),
      AcquirerSubscriptionDto(
        acquirerSubscriptionId = UUID.randomUUID(),
        acquirerId = UUID.randomUUID(),
        eventType = EventType.DEATH_NOTIFICATION,
        enrichmentFields = listOf(DeathNotificationField.FIRST_NAMES),
        enrichmentFieldsIncludedInPoll = false,
        whenCreated = LocalDateTime.now(),
      ),
    )

    every { acquirersService.getAcquirerSubscriptions() }.returns(acquirerSubscriptionDtos)

    val acquirerSubscriptionsOutput = underTest.getAcquirerSubscriptions().toList()

    assertThat(acquirerSubscriptionsOutput).hasSize(3)
    assertThat(acquirerSubscriptionsOutput).isEqualTo(acquirerSubscriptionDtos.toList())
  }

  @Test
  fun `getSubscriptionsForAcquirer gets acquirer subscriptions`() {
    val acquirerId = UUID.randomUUID()
    val acquirerSubscriptionDtos = listOf(
      AcquirerSubscriptionDto(
        acquirerSubscriptionId = UUID.randomUUID(),
        acquirerId = acquirerId,
        eventType = EventType.DEATH_NOTIFICATION,
        enrichmentFields = listOf(DeathNotificationField.FIRST_NAMES),
        enrichmentFieldsIncludedInPoll = false,
        whenCreated = LocalDateTime.now(),
      ),
      AcquirerSubscriptionDto(
        acquirerSubscriptionId = UUID.randomUUID(),
        acquirerId = acquirerId,
        eventType = EventType.DEATH_NOTIFICATION,
        enrichmentFields = listOf(DeathNotificationField.FIRST_NAMES),
        enrichmentFieldsIncludedInPoll = false,
        whenCreated = LocalDateTime.now(),
      ),
      AcquirerSubscriptionDto(
        acquirerSubscriptionId = UUID.randomUUID(),
        acquirerId = acquirerId,
        eventType = EventType.DEATH_NOTIFICATION,
        enrichmentFields = listOf(DeathNotificationField.FIRST_NAMES),
        enrichmentFieldsIncludedInPoll = false,
        whenCreated = LocalDateTime.now(),
      ),
    )

    every { acquirersService.getSubscriptionsForAcquirer(acquirerId) }.returns(acquirerSubscriptionDtos)

    val acquirerSubscriptionsOutput = underTest.getSubscriptionsForAcquirer(acquirerId).toList()

    assertThat(acquirerSubscriptionsOutput).hasSize(3)
    assertThat(acquirerSubscriptionsOutput).isEqualTo(acquirerSubscriptionDtos.toList())
  }

  @Test
  fun `addAcquirerSubscription adds acquirer subscription`() {
    val acquirerId = UUID.randomUUID()
    val acquirerSubscriptionRequest = AcquirerSubRequest(
      eventType = EventType.LIFE_EVENT,
      enrichmentFields = listOf(DeathNotificationField.FIRST_NAMES, DeathNotificationField.LAST_NAME),
    )
    val acquirerSubscriptionDto = AcquirerSubscriptionDto(
      acquirerSubscriptionId = UUID.randomUUID(),
      acquirerId = acquirerId,
      eventType = EventType.LIFE_EVENT,
      enrichmentFields = listOf(DeathNotificationField.FIRST_NAMES, DeathNotificationField.LAST_NAME),
      enrichmentFieldsIncludedInPoll = false,
      whenCreated = LocalDateTime.now(),
    )

    every { acquirersService.addAcquirerSubscription(acquirerId, any()) }.returns(acquirerSubscriptionDto)

    val acquirerSubscriptionOutput = underTest.addAcquirerSubscription(acquirerId, acquirerSubscriptionRequest)

    assertThat(acquirerSubscriptionOutput).isEqualTo(acquirerSubscriptionDto)
  }

  @Test
  fun `updateAcquirerSubscription updates acquirer subscription`() {
    val acquirerId = UUID.randomUUID()
    val subscriptionId = UUID.randomUUID()
    val acquirerSubscriptionRequest = AcquirerSubRequest(
      eventType = EventType.LIFE_EVENT,
      enrichmentFields = listOf(DeathNotificationField.FIRST_NAMES, DeathNotificationField.LAST_NAME),
    )
    val acquirerSubscriptionDto = AcquirerSubscriptionDto(
      acquirerId = acquirerId,
      acquirerSubscriptionId = subscriptionId,
      oauthClientId = "callbackClientId",
      eventType = EventType.LIFE_EVENT,
      enrichmentFields = listOf(DeathNotificationField.FIRST_NAMES, DeathNotificationField.LAST_NAME),
      enrichmentFieldsIncludedInPoll = false,
      whenCreated = LocalDateTime.now(),
    )

    every { acquirersService.updateAcquirerSubscription(acquirerId, subscriptionId, any()) }.returns(
      acquirerSubscriptionDto,
    )

    val acquirerSubscriptionOutput =
      underTest.updateAcquirerSubscription(acquirerId, subscriptionId, acquirerSubscriptionRequest)

    assertThat(acquirerSubscriptionOutput).isEqualTo(acquirerSubscriptionDto)
  }
}
