package uk.gov.gdx.datashare.services

import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import uk.gov.gdx.datashare.config.AcquirerSubscriptionNotFoundException
import uk.gov.gdx.datashare.config.DateTimeHandler
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.models.AcquirerRequest
import uk.gov.gdx.datashare.models.AcquirerSubRequest
import uk.gov.gdx.datashare.repositories.*
import java.time.LocalDateTime
import java.util.*

class AcquirersServiceTest {
  private val acquirerEventRepository = mockk<AcquirerEventRepository>()
  private val acquirerRepository = mockk<AcquirerRepository>()
  private val acquirerSubscriptionRepository = mockk<AcquirerSubscriptionRepository>()
  private val acquirerSubscriptionEnrichmentFieldRepository = mockk<AcquirerSubscriptionEnrichmentFieldRepository>()
  private val adminActionAlertsService = mockk<AdminActionAlertsService>()
  private val cognitoService = mockk<CognitoService>()
  private val dateTimeHandler = mockk<DateTimeHandler>()
  private val outboundEventQueueService = mockk<OutboundEventQueueService>()

  private val underTest = AcquirersService(
    acquirerEventRepository,
    acquirerRepository,
    acquirerSubscriptionRepository,
    acquirerSubscriptionEnrichmentFieldRepository,
    adminActionAlertsService,
    cognitoService,
    dateTimeHandler,
    outboundEventQueueService,
  )

  @Test
  fun `getAcquirers gets all acquirers`() {
    val savedAcquirers = listOf(
      Acquirer(name = "Acquirer1"),
      Acquirer(name = "Acquirer2"),
      Acquirer(name = "Acquirer3"),
    )

    every { acquirerRepository.findAllByWhenDeletedIsNull() }.returns(savedAcquirers)

    val acquirers = underTest.getAcquirers().toList()

    assertThat(acquirers).hasSize(3)
    assertThat(acquirers).isEqualTo(savedAcquirers.toList())
  }

  @Test
  fun `getAcquirerSubscriptions gets all acquirer subscriptions`() {
    val savedAcquirerSubscriptions = listOf(
      AcquirerSubscription(
        acquirerId = UUID.randomUUID(),
        eventType = EventType.DEATH_NOTIFICATION,
      ),
      AcquirerSubscription(
        acquirerId = UUID.randomUUID(),
        eventType = EventType.DEATH_NOTIFICATION,
      ),
      AcquirerSubscription(
        acquirerId = UUID.randomUUID(),
        eventType = EventType.DEATH_NOTIFICATION,
      ),
    )

    val enrichmentFields = savedAcquirerSubscriptions.map {
      AcquirerSubscriptionEnrichmentField(
        acquirerSubscriptionId = it.acquirerSubscriptionId,
        enrichmentField = EnrichmentField.FIRST_NAMES,
      )
    }
    savedAcquirerSubscriptions.forEach {
      every { acquirerSubscriptionEnrichmentFieldRepository.findAllByAcquirerSubscriptionId(it.id) }.returns(
        listOf(enrichmentFields.find { enrichmentField -> enrichmentField.acquirerSubscriptionId == it.id }!!),
      )
    }

    every { acquirerSubscriptionRepository.findAllByWhenDeletedIsNull() }.returns(savedAcquirerSubscriptions)

    val acquirerSubscriptions = underTest.getAcquirerSubscriptions().toList()

    assertThat(acquirerSubscriptions).hasSize(3)
    acquirerSubscriptions.forEach {
      val savedAcquirerSubscription =
        savedAcquirerSubscriptions.find { savedAcquirerSubscription -> savedAcquirerSubscription.acquirerSubscriptionId == it.acquirerSubscriptionId }
      val savedEnrichmentFields =
        enrichmentFields.filter { enrichmentField -> enrichmentField.acquirerSubscriptionId == it.acquirerSubscriptionId }
      assertThat(it.acquirerId).isEqualTo(savedAcquirerSubscription?.acquirerId)
      assertThat(it.eventType).isEqualTo(savedAcquirerSubscription?.eventType)
      assertThat(it.enrichmentFields).isEqualTo(savedEnrichmentFields.map { enrichmentField -> enrichmentField.enrichmentField })
    }
  }

  @Test
  fun `getSubscriptionsForAcquirer gets all acquirer subscriptions for id`() {
    val savedAcquirerSubscriptions = listOf(
      AcquirerSubscription(
        acquirerId = acquirer.id,
        eventType = EventType.DEATH_NOTIFICATION,
      ),
      AcquirerSubscription(
        acquirerId = acquirer.id,
        eventType = EventType.DEATH_NOTIFICATION,
      ),
      AcquirerSubscription(
        acquirerId = acquirer.id,
        eventType = EventType.DEATH_NOTIFICATION,
      ),
    )

    val enrichmentFields = savedAcquirerSubscriptions.map {
      AcquirerSubscriptionEnrichmentField(
        acquirerSubscriptionId = it.acquirerSubscriptionId,
        enrichmentField = EnrichmentField.FIRST_NAMES,
      )
    }

    every { acquirerSubscriptionRepository.findAllByAcquirerId(acquirer.id) }.returns(savedAcquirerSubscriptions)
    savedAcquirerSubscriptions.forEach {
      every { acquirerSubscriptionEnrichmentFieldRepository.findAllByAcquirerSubscriptionId(it.id) }.returns(
        listOf(enrichmentFields.find { enrichmentField -> enrichmentField.acquirerSubscriptionId == it.id }!!),
      )
    }

    val acquirerSubscriptions = underTest.getSubscriptionsForAcquirer(acquirer.id).toList()

    assertThat(acquirerSubscriptions).hasSize(3)
    acquirerSubscriptions.forEach {
      val savedAcquirerSubscription =
        savedAcquirerSubscriptions.find { savedAcquirerSubscription -> savedAcquirerSubscription.acquirerSubscriptionId == it.acquirerSubscriptionId }
      val savedEnrichmentFields =
        enrichmentFields.filter { enrichmentField -> enrichmentField.acquirerSubscriptionId == it.acquirerSubscriptionId }
      assertThat(it.acquirerId).isEqualTo(savedAcquirerSubscription?.acquirerId)
      assertThat(it.eventType).isEqualTo(savedAcquirerSubscription?.eventType)
      assertThat(it.enrichmentFields).isEqualTo(savedEnrichmentFields.map { enrichmentField -> enrichmentField.enrichmentField })
    }
  }

  @Test
  fun `addAcquirerSubscription adds new subscription if acquirer exists`() {
    every { acquirerRepository.findByIdOrNull(acquirer.id) }.returns(acquirer)
    every { acquirerSubscriptionRepository.save(any()) }.returns(acquirerSubscription)
    every {
      acquirerSubscriptionEnrichmentFieldRepository.saveAll(any<Iterable<AcquirerSubscriptionEnrichmentField>>())
    }.returns(allEnrichmentFields)
    every { adminActionAlertsService.noticeAction(any()) } just runs

    underTest.addAcquirerSubscription(acquirer.id, acquirerSubRequest)
    verify(exactly = 1) {
      acquirerSubscriptionRepository.save(
        withArg {
          assertThat(it.acquirerId).isEqualTo(acquirer.id)
          assertThat(it.oauthClientId).isEqualTo(acquirerSubRequest.oauthClientId)
          assertThat(it.eventType).isEqualTo(acquirerSubRequest.eventType)
        },
      )
    }
    verify(exactly = 1) {
      acquirerSubscriptionEnrichmentFieldRepository.saveAll(
        withArg<Iterable<AcquirerSubscriptionEnrichmentField>> {
          assertThat(it).hasSize(2)
          assertThat(it.elementAt(0).acquirerSubscriptionId).isEqualTo(it.elementAt(1).acquirerSubscriptionId)
            .isEqualTo(acquirerSubscription.id)
          assertThat(it.map { enrichmentField -> enrichmentField.enrichmentField }).isEqualTo(
            listOf(
              EnrichmentField.FIRST_NAMES,
              EnrichmentField.LAST_NAME,
            ),
          )
        },
      )
    }
  }

  @Test
  fun `addAcquirerSubscription creates a new queue`() {
    every { acquirerRepository.findByIdOrNull(acquirer.id) }.returns(acquirer)
    every { acquirerSubscriptionRepository.save(any()) }.returns(acquirerSubscription)
    every {
      acquirerSubscriptionEnrichmentFieldRepository.saveAll(any<Iterable<AcquirerSubscriptionEnrichmentField>>())
    }.returns(allEnrichmentFields)
    every { outboundEventQueueService.createAcquirerQueue(any(), any()) } returns ""
    every { adminActionAlertsService.noticeAction(any()) } just runs

    val acquirerSubRequest = AcquirerSubRequest(
      EventType.TEST_EVENT,
      oauthClientId = null,
      enrichmentFields = emptyList(),
      queueName = "acq_test",
      principalArn = "principal",
    )
    underTest.addAcquirerSubscription(acquirer.id, acquirerSubRequest)

    verify(exactly = 1) {
      outboundEventQueueService.createAcquirerQueue(
        withArg { assertThat(it).isEqualTo("acq_test") },
        withArg { assertThat(it).isEqualTo("principal") },
      )
    }
  }

  @Test
  fun `updateAcquirerSubscription updates subscription`() {
    every { acquirerRepository.findByIdOrNull(acquirer.id) }.returns(acquirer)
    every { acquirerSubscriptionRepository.findByIdOrNull(acquirerSubscription.id) }.returns(acquirerSubscription)

    every { acquirerSubscriptionRepository.save(any()) }.returns(acquirerSubscription)
    every { acquirerSubscriptionEnrichmentFieldRepository.deleteAllByAcquirerSubscriptionId(acquirerSubscription.id) }.returns(
      Unit,
    )
    every {
      acquirerSubscriptionEnrichmentFieldRepository.saveAll(any<Iterable<AcquirerSubscriptionEnrichmentField>>())
    }.returns(allEnrichmentFields)
    every { adminActionAlertsService.noticeAction(any()) } just runs

    underTest.updateAcquirerSubscription(acquirer.id, acquirerSubscription.id, acquirerSubRequest)

    verify(exactly = 1) {
      acquirerSubscriptionRepository.save(
        withArg {
          assertThat(it.acquirerId).isEqualTo(acquirer.id)
          assertThat(it.oauthClientId).isEqualTo(acquirerSubRequest.oauthClientId)
          assertThat(it.eventType).isEqualTo(acquirerSubRequest.eventType)
        },
      )
    }
    verify(exactly = 1) {
      acquirerSubscriptionEnrichmentFieldRepository.saveAll(
        withArg<Iterable<AcquirerSubscriptionEnrichmentField>> {
          assertThat(it).hasSize(2)
          assertThat(it.elementAt(0).acquirerSubscriptionId).isEqualTo(it.elementAt(1).acquirerSubscriptionId)
            .isEqualTo(acquirerSubscription.id)
          assertThat(it.map { enrichmentField -> enrichmentField.enrichmentField }).isEqualTo(
            listOf(
              EnrichmentField.FIRST_NAMES,
              EnrichmentField.LAST_NAME,
            ),
          )
        },
      )
    }
  }

  @Test
  fun `updateAcquirerSubscription does not update subscription if subscription does not exist`() {
    every { acquirerSubscriptionRepository.findByIdOrNull(acquirerSubscription.id) }.returns(null)
    every { adminActionAlertsService.noticeAction(any()) } just runs

    val exception = assertThrows<AcquirerSubscriptionNotFoundException> {
      underTest.updateAcquirerSubscription(acquirer.id, acquirerSubscription.id, acquirerSubRequest)
    }

    assertThat(exception.message).isEqualTo("Subscription ${acquirerSubscription.id} not found")

    verify(exactly = 0) { acquirerSubscriptionRepository.save(any()) }
  }

  @Test
  fun `deleteAcquirerSubscription deletes subscription, enrichment fields and cognito client`() {
    val now = LocalDateTime.now()
    every { dateTimeHandler.now() } returns now

    every { acquirerEventRepository.softDeleteAllByAcquirerSubscriptionId(acquirerSubscription.id, now) } just runs

    every { acquirerSubscriptionRepository.findByIdOrNull(acquirerSubscription.id) } returns acquirerSubscription
    every { acquirerSubscriptionRepository.findAllByOauthClientIdAndWhenDeletedIsNull(acquirerSubscription.oauthClientId!!) } returns emptyList()
    every { acquirerSubscriptionRepository.save(any()) } returns acquirerSubscription

    every { acquirerSubscriptionEnrichmentFieldRepository.deleteAllByAcquirerSubscriptionId(acquirerSubscription.id) } returns Unit

    every { adminActionAlertsService.noticeAction(any()) } just runs

    every { cognitoService.deleteUserPoolClient(acquirerSubscription.oauthClientId!!) } just runs

    underTest.deleteAcquirerSubscription(acquirerSubscription.id)

    verify(exactly = 1) {
      acquirerSubscriptionRepository.save(
        withArg {
          assertThat(it.whenDeleted).isEqualTo(now)
        },
      )
    }

    verify(exactly = 1) {
      acquirerSubscriptionEnrichmentFieldRepository.deleteAllByAcquirerSubscriptionId(acquirerSubscription.id)
    }

    verify(exactly = 1) {
      acquirerEventRepository.softDeleteAllByAcquirerSubscriptionId(acquirerSubscription.id, now)
    }

    verify(exactly = 1) {
      cognitoService.deleteUserPoolClient(acquirerSubscription.oauthClientId!!)
    }
  }

  @Test
  fun `deleteAcquirerSubscription deletes subscription, queue and dlq`() {
    val now = LocalDateTime.now()
    every { dateTimeHandler.now() } returns now

    every { acquirerEventRepository.softDeleteAllByAcquirerSubscriptionId(queueAcquirerSubscription.id, now) } just runs

    every { acquirerSubscriptionRepository.findByIdOrNull(queueAcquirerSubscription.id) } returns queueAcquirerSubscription
    every { acquirerSubscriptionRepository.findAllByQueueNameAndWhenDeletedIsNull(queueAcquirerSubscription.queueName!!) } returns emptyList()
    every { acquirerSubscriptionRepository.save(any()) } returns queueAcquirerSubscription

    every { acquirerSubscriptionEnrichmentFieldRepository.deleteAllByAcquirerSubscriptionId(queueAcquirerSubscription.id) } returns Unit

    every { adminActionAlertsService.noticeAction(any()) } just runs

    every { outboundEventQueueService.deleteAcquirerQueueAndDlq(queueAcquirerSubscription.queueName!!) } just runs

    underTest.deleteAcquirerSubscription(queueAcquirerSubscription.id)

    verify(exactly = 1) {
      acquirerSubscriptionRepository.save(
        withArg {
          assertThat(it.whenDeleted).isEqualTo(now)
        },
      )
    }

    verify(exactly = 1) {
      acquirerSubscriptionEnrichmentFieldRepository.deleteAllByAcquirerSubscriptionId(queueAcquirerSubscription.id)
    }

    verify(exactly = 1) {
      acquirerEventRepository.softDeleteAllByAcquirerSubscriptionId(queueAcquirerSubscription.id, now)
    }

    verify(exactly = 1) {
      outboundEventQueueService.deleteAcquirerQueueAndDlq(queueAcquirerSubscription.queueName!!)
    }
  }

  @Test
  fun `deleteAcquirerSubscription deletes subscription and enrichment fields but not cognito client if shared`() {
    val now = LocalDateTime.now()
    every { dateTimeHandler.now() } returns now

    every { acquirerEventRepository.softDeleteAllByAcquirerSubscriptionId(acquirerSubscription.id, now) } just runs

    every { acquirerSubscriptionRepository.findByIdOrNull(acquirerSubscription.id) } returns acquirerSubscription
    every { acquirerSubscriptionRepository.findAllByOauthClientIdAndWhenDeletedIsNull(acquirerSubscription.oauthClientId!!) } returns listOf(
      otherAcquirerSubscription,
    )
    every { acquirerSubscriptionRepository.save(any()) } returns acquirerSubscription

    every { acquirerSubscriptionEnrichmentFieldRepository.deleteAllByAcquirerSubscriptionId(acquirerSubscription.id) } returns Unit

    every { adminActionAlertsService.noticeAction(any()) } just runs

    every { cognitoService.deleteUserPoolClient(acquirerSubscription.oauthClientId!!) } just runs

    underTest.deleteAcquirerSubscription(acquirerSubscription.id)

    verify(exactly = 1) {
      acquirerSubscriptionRepository.save(
        withArg {
          assertThat(it.whenDeleted).isEqualTo(now)
        },
      )
    }

    verify(exactly = 1) {
      acquirerSubscriptionEnrichmentFieldRepository.deleteAllByAcquirerSubscriptionId(acquirerSubscription.id)
    }

    verify(exactly = 1) {
      acquirerEventRepository.softDeleteAllByAcquirerSubscriptionId(acquirerSubscription.id, now)
    }

    verify(exactly = 0) {
      cognitoService.deleteUserPoolClient(acquirerSubscription.oauthClientId!!)
    }
  }

  @Test
  fun `deleteAcquirerSubscription deletes subscription but not queue and dlq if shared`() {
    val now = LocalDateTime.now()
    every { dateTimeHandler.now() } returns now

    every { acquirerEventRepository.softDeleteAllByAcquirerSubscriptionId(queueAcquirerSubscription.id, now) } just runs

    every { acquirerSubscriptionRepository.findByIdOrNull(queueAcquirerSubscription.id) } returns queueAcquirerSubscription
    every { acquirerSubscriptionRepository.findAllByQueueNameAndWhenDeletedIsNull(queueAcquirerSubscription.queueName!!) } returns listOf(
      otherQueueAcquirerSubscription,
    )
    every { acquirerSubscriptionRepository.save(any()) } returns queueAcquirerSubscription

    every { acquirerSubscriptionEnrichmentFieldRepository.deleteAllByAcquirerSubscriptionId(queueAcquirerSubscription.id) } returns Unit

    every { adminActionAlertsService.noticeAction(any()) } just runs

    every { outboundEventQueueService.deleteAcquirerQueueAndDlq(queueAcquirerSubscription.queueName!!) } just runs
    every { outboundEventQueueService.deleteAcquirerQueueAndDlq("${queueAcquirerSubscription.queueName}_dlq") } just runs

    underTest.deleteAcquirerSubscription(queueAcquirerSubscription.id)

    verify(exactly = 1) {
      acquirerSubscriptionRepository.save(
        withArg {
          assertThat(it.whenDeleted).isEqualTo(now)
        },
      )
    }

    verify(exactly = 1) {
      acquirerSubscriptionEnrichmentFieldRepository.deleteAllByAcquirerSubscriptionId(queueAcquirerSubscription.id)
    }

    verify(exactly = 1) {
      acquirerEventRepository.softDeleteAllByAcquirerSubscriptionId(queueAcquirerSubscription.id, now)
    }

    verify(exactly = 0) {
      outboundEventQueueService.deleteAcquirerQueueAndDlq(queueAcquirerSubscription.queueName!!)
    }

    verify(exactly = 0) {
      outboundEventQueueService.deleteAcquirerQueueAndDlq("${queueAcquirerSubscription.queueName}_dlq")
    }
  }

  @Test
  fun `deleteAcquirerSubscription does not delete subscription if subscription does not exist`() {
    val now = LocalDateTime.now()
    every { dateTimeHandler.now() } returns now

    every { acquirerEventRepository.softDeleteAllByAcquirerSubscriptionId(acquirerSubscription.id, now) } just runs
    every { acquirerSubscriptionRepository.findByIdOrNull(acquirerSubscription.id) } returns null
    every { adminActionAlertsService.noticeAction(any()) } just runs

    val exception = assertThrows<AcquirerSubscriptionNotFoundException> {
      underTest.deleteAcquirerSubscription(acquirerSubscription.id)
    }

    assertThat(exception.message).isEqualTo("Subscription ${acquirerSubscription.id} not found")

    verify(exactly = 0) { acquirerSubscriptionRepository.save(any()) }

    verify(exactly = 0) { acquirerSubscriptionEnrichmentFieldRepository.deleteAllByAcquirerSubscriptionId(any()) }

    verify(exactly = 0) { acquirerEventRepository.softDeleteAllByAcquirerSubscriptionId(any(), any()) }

    verify(exactly = 0) { cognitoService.deleteUserPoolClient(any()) }
  }

  @Test
  fun `addAcquirer adds acquirer`() {
    every { adminActionAlertsService.noticeAction(any()) } just runs
    val acquirerRequest = AcquirerRequest(
      name = "Acquirer",
    )

    every { acquirerRepository.save(any()) }.returns(acquirer)

    underTest.addAcquirer(acquirerRequest)

    verify(exactly = 1) {
      acquirerRepository.save(
        withArg {
          assertThat(it.name).isEqualTo(acquirerRequest.name)
        },
      )
    }
  }

  @Test
  fun `addAcquirer action is noticed`() {
    every { adminActionAlertsService.noticeAction(any()) } just runs
    val acquirerRequest = AcquirerRequest(
      name = "Acquirer",
    )

    every { acquirerRepository.save(any()) }.returns(acquirer)

    underTest.addAcquirer(acquirerRequest)

    verify(exactly = 1) {
      adminActionAlertsService.noticeAction(
        withArg {
          assertThat(it.name).isEqualTo("Add acquirer")
          assertThat(it.details).isEqualTo(acquirerRequest)
        },
      )
    }
  }

  @Test
  fun `deleteAcquirer deletes acquirer, subscription, enrichment fields, and cognito client`() {
    val now = LocalDateTime.now()
    every { dateTimeHandler.now() } returns now

    every { acquirerEventRepository.softDeleteAllByAcquirerSubscriptionId(acquirerSubscription.id, now) } just runs

    every { acquirerRepository.findByIdOrNull(acquirer.id) } returns acquirer
    every { acquirerRepository.save(any()) } returns acquirer

    every { acquirerSubscriptionRepository.findByIdOrNull(acquirerSubscription.id) } returns acquirerSubscription
    every { acquirerSubscriptionRepository.findAllByOauthClientIdAndWhenDeletedIsNull(acquirerSubscription.oauthClientId!!) } returns emptyList()
    every { acquirerSubscriptionRepository.findAllByAcquirerId(acquirer.id) } returns listOf(acquirerSubscription)
    every { acquirerSubscriptionRepository.save(any()) } returns acquirerSubscription

    every { acquirerSubscriptionEnrichmentFieldRepository.deleteAllByAcquirerSubscriptionId(acquirerSubscription.id) } returns Unit

    every { adminActionAlertsService.noticeAction(any()) } just runs

    every { cognitoService.deleteUserPoolClient(acquirerSubscription.oauthClientId!!) } just runs

    underTest.deleteAcquirer(acquirer.id)

    verify(exactly = 1) {
      acquirerRepository.save(
        withArg {
          assertThat(it.whenDeleted).isEqualTo(now)
        },
      )
    }

    verify(exactly = 1) {
      acquirerSubscriptionRepository.save(
        withArg {
          assertThat(it.whenDeleted).isEqualTo(now)
        },
      )
    }

    verify(exactly = 1) {
      acquirerSubscriptionEnrichmentFieldRepository.deleteAllByAcquirerSubscriptionId(acquirerSubscription.id)
    }

    verify(exactly = 1) {
      acquirerEventRepository.softDeleteAllByAcquirerSubscriptionId(acquirerSubscription.id, now)
    }

    verify(exactly = 1) {
      cognitoService.deleteUserPoolClient(acquirerSubscription.oauthClientId!!)
    }
  }

  @Test
  fun `deleteAcquirer deletes acquirer, multiple subscriptions, enrichment fields, and cognito client`() {
    val now = LocalDateTime.now()
    every { dateTimeHandler.now() } returns now

    every { acquirerEventRepository.softDeleteAllByAcquirerSubscriptionId(any(), now) } just runs

    every { acquirerRepository.findByIdOrNull(acquirer.id) } returns acquirer
    every { acquirerRepository.save(any()) } returns acquirer

    every { acquirerSubscriptionRepository.findByIdOrNull(acquirerSubscription.id) } returns acquirerSubscription
    every { acquirerSubscriptionRepository.findByIdOrNull(otherAcquirerSubscription.id) } returns otherAcquirerSubscription
    every { acquirerSubscriptionRepository.findAllByOauthClientIdAndWhenDeletedIsNull(acquirerSubscription.oauthClientId!!) } returnsMany
      listOf(
        listOf(
          otherAcquirerSubscription,
        ),
        emptyList(),
      )
    every { acquirerSubscriptionRepository.findAllByAcquirerId(acquirer.id) } returns listOf(
      acquirerSubscription,
      otherAcquirerSubscription,
    )
    every { acquirerSubscriptionRepository.save(any()) } returns acquirerSubscription

    every { acquirerSubscriptionEnrichmentFieldRepository.deleteAllByAcquirerSubscriptionId(any()) } returns Unit

    every { adminActionAlertsService.noticeAction(any()) } just runs

    every { cognitoService.deleteUserPoolClient(acquirerSubscription.oauthClientId!!) } just runs

    underTest.deleteAcquirer(acquirer.id)

    verify(exactly = 1) {
      acquirerRepository.save(
        withArg {
          assertThat(it.whenDeleted).isEqualTo(now)
        },
      )
    }

    verify(exactly = 1) {
      acquirerSubscriptionRepository.save(
        withArg {
          assertThat(it.id).isEqualTo(acquirerSubscription.id)
          assertThat(it.whenDeleted).isEqualTo(now)
        },
      )
    }

    verify(exactly = 1) {
      acquirerSubscriptionRepository.save(
        withArg {
          assertThat(it.id).isEqualTo(otherAcquirerSubscription.id)
          assertThat(it.whenDeleted).isEqualTo(now)
        },
      )
    }

    verify(exactly = 1) {
      acquirerSubscriptionEnrichmentFieldRepository.deleteAllByAcquirerSubscriptionId(acquirerSubscription.id)
    }
    verify(exactly = 1) {
      acquirerSubscriptionEnrichmentFieldRepository.deleteAllByAcquirerSubscriptionId(otherAcquirerSubscription.id)
    }

    verify(exactly = 1) {
      acquirerEventRepository.softDeleteAllByAcquirerSubscriptionId(acquirerSubscription.id, now)
    }
    verify(exactly = 1) {
      acquirerEventRepository.softDeleteAllByAcquirerSubscriptionId(otherAcquirerSubscription.id, now)
    }

    verify(exactly = 1) {
      cognitoService.deleteUserPoolClient(acquirerSubscription.oauthClientId!!)
    }
  }

  @Test
  fun `deleteAcquirer deletes acquirer, subscription, queue and dlq`() {
    val now = LocalDateTime.now()
    every { dateTimeHandler.now() }.returns(now)

    every { acquirerEventRepository.softDeleteAllByAcquirerSubscriptionId(queueAcquirerSubscription.id, now) } just runs

    every { acquirerRepository.findByIdOrNull(acquirer.id) } returns acquirer
    every { acquirerRepository.save(any()) } returns acquirer

    every { acquirerSubscriptionRepository.findByIdOrNull(queueAcquirerSubscription.id) } returns queueAcquirerSubscription
    every { acquirerSubscriptionRepository.findAllByQueueNameAndWhenDeletedIsNull(queueAcquirerSubscription.queueName!!) } returns emptyList()
    every { acquirerSubscriptionRepository.findAllByAcquirerId(acquirer.id) } returns listOf(queueAcquirerSubscription)
    every { acquirerSubscriptionRepository.save(any()) } returns queueAcquirerSubscription

    every { acquirerSubscriptionEnrichmentFieldRepository.deleteAllByAcquirerSubscriptionId(queueAcquirerSubscription.id) } returns Unit

    every { adminActionAlertsService.noticeAction(any()) } just runs

    every { outboundEventQueueService.deleteAcquirerQueueAndDlq(queueAcquirerSubscription.queueName!!) } just runs
    every { outboundEventQueueService.deleteAcquirerQueueAndDlq("${queueAcquirerSubscription.queueName}_dlq") } just runs

    underTest.deleteAcquirer(acquirer.id)

    verify(exactly = 1) {
      acquirerRepository.save(
        withArg {
          assertThat(it.whenDeleted).isEqualTo(now)
        },
      )
    }

    verify(exactly = 1) {
      acquirerSubscriptionRepository.save(
        withArg {
          assertThat(it.whenDeleted).isEqualTo(now)
        },
      )
    }

    verify(exactly = 1) {
      acquirerSubscriptionEnrichmentFieldRepository.deleteAllByAcquirerSubscriptionId(queueAcquirerSubscription.id)
    }

    verify(exactly = 1) {
      acquirerEventRepository.softDeleteAllByAcquirerSubscriptionId(queueAcquirerSubscription.id, now)
    }

    verify(exactly = 1) {
      outboundEventQueueService.deleteAcquirerQueueAndDlq(queueAcquirerSubscription.queueName!!)
    }
  }

  @Test
  fun `deleteAcquirer deletes acquirer, multiple subscriptions, queue and dlq`() {
    val now = LocalDateTime.now()
    every { dateTimeHandler.now() } returns now

    every { acquirerEventRepository.softDeleteAllByAcquirerSubscriptionId(any(), now) } just runs

    every { acquirerRepository.findByIdOrNull(acquirer.id) } returns acquirer
    every { acquirerRepository.save(any()) } returns acquirer

    every { acquirerSubscriptionRepository.findByIdOrNull(queueAcquirerSubscription.id) } returns queueAcquirerSubscription
    every { acquirerSubscriptionRepository.findByIdOrNull(otherQueueAcquirerSubscription.id) } returns otherQueueAcquirerSubscription
    every { acquirerSubscriptionRepository.findAllByQueueNameAndWhenDeletedIsNull(queueAcquirerSubscription.queueName!!) } returnsMany
      listOf(
        listOf(
          otherQueueAcquirerSubscription,
        ),
        emptyList(),
      )
    every { acquirerSubscriptionRepository.findAllByAcquirerId(acquirer.id) } returns
      listOf(
        queueAcquirerSubscription,
        otherQueueAcquirerSubscription,
      )
    every { acquirerSubscriptionRepository.save(any()) } returnsMany
      listOf(queueAcquirerSubscription, otherQueueAcquirerSubscription)

    every { acquirerSubscriptionEnrichmentFieldRepository.deleteAllByAcquirerSubscriptionId(any()) } returns Unit

    every { adminActionAlertsService.noticeAction(any()) } just runs

    every { outboundEventQueueService.deleteAcquirerQueueAndDlq(queueAcquirerSubscription.queueName!!) } just runs
    every { outboundEventQueueService.deleteAcquirerQueueAndDlq("${queueAcquirerSubscription.queueName}_dlq") } just runs

    underTest.deleteAcquirer(acquirer.id)

    verify(exactly = 1) {
      acquirerRepository.save(
        withArg {
          assertThat(it.whenDeleted).isEqualTo(now)
        },
      )
    }

    verify(exactly = 1) {
      acquirerSubscriptionRepository.save(
        withArg {
          assertThat(it.id).isEqualTo(queueAcquirerSubscription.id)
          assertThat(it.whenDeleted).isEqualTo(now)
        },
      )
    }

    verify(exactly = 1) {
      acquirerSubscriptionRepository.save(
        withArg {
          assertThat(it.id).isEqualTo(otherQueueAcquirerSubscription.id)
          assertThat(it.whenDeleted).isEqualTo(now)
        },
      )
    }

    verify(exactly = 1) {
      acquirerSubscriptionEnrichmentFieldRepository.deleteAllByAcquirerSubscriptionId(queueAcquirerSubscription.id)
    }
    verify(exactly = 1) {
      acquirerSubscriptionEnrichmentFieldRepository.deleteAllByAcquirerSubscriptionId(otherQueueAcquirerSubscription.id)
    }

    verify(exactly = 1) {
      acquirerEventRepository.softDeleteAllByAcquirerSubscriptionId(queueAcquirerSubscription.id, now)
    }
    verify(exactly = 1) {
      acquirerEventRepository.softDeleteAllByAcquirerSubscriptionId(otherQueueAcquirerSubscription.id, now)
    }

    verify(exactly = 1) {
      outboundEventQueueService.deleteAcquirerQueueAndDlq(queueAcquirerSubscription.queueName!!)
    }
  }

  @Test
  fun `deleteAcquirer action is noticed`() {
    val now = LocalDateTime.now()
    every { dateTimeHandler.now() } returns now

    every { acquirerEventRepository.softDeleteAllByAcquirerSubscriptionId(any(), now) } just runs

    every { acquirerRepository.findByIdOrNull(acquirer.id) } returns acquirer
    every { acquirerRepository.save(any()) } returns acquirer

    every { acquirerSubscriptionRepository.findByIdOrNull(acquirerSubscription.id) } returns acquirerSubscription
    every { acquirerSubscriptionRepository.findAllByOauthClientIdAndWhenDeletedIsNull(acquirerSubscription.oauthClientId!!) } returns emptyList()
    every { acquirerSubscriptionRepository.findAllByAcquirerId(acquirer.id) } returns listOf(acquirerSubscription)
    every { acquirerSubscriptionRepository.save(any()) } returns acquirerSubscription

    every { acquirerSubscriptionEnrichmentFieldRepository.deleteAllByAcquirerSubscriptionId(acquirerSubscription.id) } returns Unit

    every { adminActionAlertsService.noticeAction(any()) } just runs

    every { cognitoService.deleteUserPoolClient(acquirerSubscription.oauthClientId!!) } just runs

    underTest.deleteAcquirer(acquirer.id)

    verify(exactly = 1) {
      adminActionAlertsService.noticeAction(
        withArg {
          assertThat(it.name).isEqualTo("Delete acquirer")
          assertThat(it.details.getProperty("acquirerId")).isEqualTo(acquirer.id)
          assertThat(it.details.getProperty("whenDeleted")).isEqualTo(now)
        },
      )
    }

    verify(exactly = 1) {
      adminActionAlertsService.noticeAction(
        withArg {
          assertThat(it.name).isEqualTo("Delete acquirer subscription")
          assertThat(it.details.getProperty("subscriptionId")).isEqualTo(acquirerSubscription.id)
          assertThat(it.details.getProperty("whenDeleted")).isEqualTo(now)
        },
      )
    }
  }

  @Test
  fun `addAcquirerSubscription action is noticed`() {
    every { acquirerRepository.findByIdOrNull(acquirer.id) }.returns(acquirer)
    every { acquirerSubscriptionRepository.save(any()) }.returns(acquirerSubscription)
    every {
      acquirerSubscriptionEnrichmentFieldRepository.saveAll(any<Iterable<AcquirerSubscriptionEnrichmentField>>())
    }.returns(allEnrichmentFields)
    every { adminActionAlertsService.noticeAction(any()) } just runs

    underTest.addAcquirerSubscription(acquirer.id, acquirerSubRequest)

    verify(exactly = 1) {
      adminActionAlertsService.noticeAction(
        withArg {
          assertThat(it.name).isEqualTo("Add acquirer subscription")
          assertThat(it.details.getProperty("acquirerId")).isEqualTo(acquirer.id)
          assertThat(it.details.getProperty("acquirerSubRequest")).isEqualTo(acquirerSubRequest)
        },
      )
    }
  }

  @Test
  fun `updateAcquirerSubscription action is noticed`() {
    every { acquirerRepository.findByIdOrNull(acquirer.id) }.returns(acquirer)
    every { acquirerSubscriptionRepository.findByIdOrNull(acquirerSubscription.id) }.returns(acquirerSubscription)

    every { acquirerSubscriptionRepository.save(any()) }.returns(acquirerSubscription)
    every { acquirerSubscriptionEnrichmentFieldRepository.deleteAllByAcquirerSubscriptionId(acquirerSubscription.id) }.returns(
      Unit,
    )
    every {
      acquirerSubscriptionEnrichmentFieldRepository.saveAll(any<Iterable<AcquirerSubscriptionEnrichmentField>>())
    }.returns(allEnrichmentFields)
    every { adminActionAlertsService.noticeAction(any()) } just runs

    underTest.updateAcquirerSubscription(acquirer.id, acquirerSubscription.id, acquirerSubRequest)

    verify(exactly = 1) {
      adminActionAlertsService.noticeAction(
        withArg {
          assertThat(it.name).isEqualTo("Update acquirer subscription")
          assertThat(it.details.getProperty("acquirerId")).isEqualTo(acquirer.id)
          assertThat(it.details.getProperty("subscriptionId")).isEqualTo(acquirerSubscription.id)
          assertThat(it.details.getProperty("acquirerSubRequest")).isEqualTo(acquirerSubRequest)
        },
      )
    }
  }

  private val acquirer = Acquirer(name = "Base Acquirer")
  private val acquirerSubscription = AcquirerSubscription(
    acquirerId = acquirer.id,
    oauthClientId = "pollClientId",
    eventType = EventType.DEATH_NOTIFICATION,
  )
  private val otherAcquirerSubscription = AcquirerSubscription(
    acquirerId = acquirer.id,
    oauthClientId = "Client",
    eventType = EventType.DEATH_NOTIFICATION,
  )
  private val testEventAcquirerSubscription = AcquirerSubscription(
    acquirerId = acquirer.id,
    oauthClientId = "",
    eventType = EventType.TEST_EVENT,
  )
  private val queueAcquirerSubscription = AcquirerSubscription(
    acquirerId = acquirer.id,
    queueName = "Queue",
    eventType = EventType.DEATH_NOTIFICATION,
  )
  private val otherQueueAcquirerSubscription = AcquirerSubscription(
    acquirerId = acquirer.id,
    queueName = "Queue",
    eventType = EventType.DEATH_NOTIFICATION,
  )
  private val acquirerSubRequest = AcquirerSubRequest(
    eventType = EventType.LIFE_EVENT,
    enrichmentFields = listOf(EnrichmentField.FIRST_NAMES, EnrichmentField.LAST_NAME),
    oauthClientId = "callbackClientIdNew",
  )
  private val acquirerSubscriptionEnrichmentField = AcquirerSubscriptionEnrichmentField(
    acquirerSubscriptionId = acquirerSubscription.id,
    enrichmentField = EnrichmentField.FIRST_NAMES,
  )
  private val newAcquirerSubscriptionEnrichmentField = AcquirerSubscriptionEnrichmentField(
    acquirerSubscriptionId = acquirerSubscription.id,
    enrichmentField = EnrichmentField.LAST_NAME,
  )
  private val allEnrichmentFields = listOf(acquirerSubscriptionEnrichmentField, newAcquirerSubscriptionEnrichmentField)
}
