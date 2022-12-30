package uk.gov.gdx.datashare.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.gdx.datashare.config.JacksonConfiguration
import uk.gov.gdx.datashare.repository.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class DeathNotificationServiceTest {
  private val consumerSubscriptionRepository = mockk<ConsumerSubscriptionRepository>()
  private val egressEventDataRepository = mockk<EgressEventDataRepository>()
  private val eventPublishingService = mockk<EventPublishingService>()
  private val levApiService = mockk<LevApiService>()
  private val objectMapper = JacksonConfiguration().objectMapper()

  private val underTest = DeathNotificationService(
    consumerSubscriptionRepository,
    egressEventDataRepository,
    eventPublishingService,
    levApiService,
    objectMapper
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

  @Test
  fun `saveDeathNotificationEvents saves full death notifications for LEV`() {
    runBlocking {
      val dataProcessorMessage = DataProcessorMessage(
        datasetId = "DEATH_LEV",
        eventTypeId = "DEATH_NOTIFICATION",
        eventTime = LocalDateTime.of(2010, 1, 1, 12, 0),
        publisher = "HMPO",
        storePayload = false,
        subscriptionId = UUID.randomUUID(),
        id = "123456789",
        details = null,
      )
      val dataDetail = DataDetail(id = dataProcessorMessage.id!!, data = null)
      val deathRecord = DeathRecord(
        deceased = Deceased(
          forenames = "Alice",
          surname = "Smith",
          dateOfBirth = LocalDate.of(1920, 1, 1),
          dateOfDeath = LocalDate.of(2010, 1, 1),
          sex = "female",
          address = "666 Inform House, 6 Inform street, Informington, Informshire",
          dateOfDeathQualifier = null
        ),
        id = dataDetail.id,
        date = LocalDate.now(),
        partner = null
      )

      coEvery { consumerSubscriptionRepository.findAllByIngressEventType(ingressEventData.eventTypeId) }
        .returns(consumerSubscriptions)
      coEvery { levApiService.findDeathById(dataDetail.id.toInt()) }.returns(flowOf(deathRecord))
      coEvery { egressEventDataRepository.saveAll(any<Iterable<EgressEventData>>()) }.returns(fakeSavedEvents)
      coEvery { eventPublishingService.storeAndPublishEvent(any(), any()) }.returns(Unit)

      underTest.saveDeathNotificationEvents(ingressEventData, dataDetail, dataProcessorMessage)

      coVerify(exactly = 1) {
        egressEventDataRepository.saveAll(withArg<Iterable<EgressEventData>> {
          assertThat(it).hasSize(2)
          val simpleEvent = it.find { event -> event.consumerSubscriptionId == simpleSubscription.id }
          val complexEvent = it.find { event -> event.consumerSubscriptionId == complexSubscription.id }

          assertThat(simpleEvent?.ingressEventId).isEqualTo(complexEvent?.ingressEventId)
            .isEqualTo(ingressEventData.eventId)
          assertThat(simpleEvent?.datasetId).isEqualTo(complexEvent?.datasetId)
            .isEqualTo(dataProcessorMessage.datasetId)
          assertThat(simpleEvent?.dataId).isEqualTo(complexEvent?.dataId)
            .isEqualTo(dataDetail.id)
          assertThat(simpleEvent?.whenCreated).isEqualTo(complexEvent?.whenCreated)
            .isEqualTo(dataProcessorMessage.eventTime)
          assertThat(simpleEvent?.dataExpiryTime).isEqualTo(complexEvent?.dataExpiryTime)
            .isEqualTo(dataProcessorMessage.eventTime.plusHours(1))

          assertThat(simpleEvent?.dataPayload)
            .isEqualTo(objectMapper.writeValueAsString(simpleDeathNotificationDetails))
          assertThat(complexEvent?.dataPayload)
            .isEqualTo(objectMapper.writeValueAsString(complexDeathNotificationDetails))
        })
      }
      fakeSavedEvents.collect {
        coVerify(exactly = 1) { eventPublishingService.storeAndPublishEvent(it.id, dataProcessorMessage) }
      }
    }
  }

  @Test
  fun `saveDeathNotificationEvents saves full death notifications for CSV`() {
    runBlocking {
      val dataProcessorMessage = DataProcessorMessage(
        datasetId = "DEATH_CSV",
        eventTypeId = "DEATH_NOTIFICATION",
        eventTime = LocalDateTime.of(2010, 1, 1, 12, 0),
        publisher = "HMPO",
        storePayload = true,
        subscriptionId = UUID.randomUUID(),
        id = "123456789",
        details = "Smith,Alice,1920-01-01,2010-01-01,female,\"666 Inform House, 6 Inform street, Informington, Informshire\"",
      )
      val dataDetail = DataDetail(
        id = dataProcessorMessage.id!!,
        data = "Smith,Alice,1920-01-01,2010-01-01,female,\"666 Inform House, 6 Inform street, Informington, Informshire\""
      )

      coEvery { consumerSubscriptionRepository.findAllByIngressEventType(ingressEventData.eventTypeId) }
        .returns(consumerSubscriptions)
      coEvery { egressEventDataRepository.saveAll(any<Iterable<EgressEventData>>()) }.returns(fakeSavedEvents)
      coEvery { eventPublishingService.storeAndPublishEvent(any(), any()) }.returns(Unit)

      underTest.saveDeathNotificationEvents(ingressEventData, dataDetail, dataProcessorMessage)

      coVerify(exactly = 1) {
        egressEventDataRepository.saveAll(withArg<Iterable<EgressEventData>> {
          assertThat(it).hasSize(2)
          val simpleEvent = it.find { event -> event.consumerSubscriptionId == simpleSubscription.id }
          val complexEvent = it.find { event -> event.consumerSubscriptionId == complexSubscription.id }

          assertThat(simpleEvent?.ingressEventId).isEqualTo(complexEvent?.ingressEventId)
            .isEqualTo(ingressEventData.eventId)
          assertThat(simpleEvent?.datasetId).isEqualTo(complexEvent?.datasetId)
            .isEqualTo(dataProcessorMessage.datasetId)
          assertThat(simpleEvent?.dataId).isEqualTo(complexEvent?.dataId)
            .isEqualTo(dataDetail.id)
          assertThat(simpleEvent?.whenCreated).isEqualTo(complexEvent?.whenCreated)
            .isEqualTo(dataProcessorMessage.eventTime)
          assertThat(simpleEvent?.dataExpiryTime).isEqualTo(complexEvent?.dataExpiryTime)
            .isEqualTo(dataProcessorMessage.eventTime.plusHours(1))

          assertThat(simpleEvent?.dataPayload)
            .isEqualTo(objectMapper.writeValueAsString(simpleDeathNotificationDetails))
          assertThat(complexEvent?.dataPayload)
            .isEqualTo(objectMapper.writeValueAsString(complexDeathNotificationDetails))
        })
      }
      fakeSavedEvents.collect {
        coVerify(exactly = 1) { eventPublishingService.storeAndPublishEvent(it.id, dataProcessorMessage) }
      }
    }
  }

  @Test
  fun `saveDeathNotificationEvents saves no death deatils for Pass Through`() {
    runBlocking {
      val dataProcessorMessage = DataProcessorMessage(
        datasetId = "PASS_THROUGH",
        eventTypeId = "DEATH_NOTIFICATION",
        eventTime = LocalDateTime.of(2010, 1, 1, 12, 0),
        publisher = "HMPO",
        storePayload = true,
        subscriptionId = UUID.randomUUID(),
        id = "123456789",
        details = null
      )
      val dataDetail = DataDetail(id = dataProcessorMessage.id!!, data = null)

      coEvery { consumerSubscriptionRepository.findAllByIngressEventType(ingressEventData.eventTypeId) }
        .returns(consumerSubscriptions)
      coEvery { egressEventDataRepository.saveAll(any<Iterable<EgressEventData>>()) }.returns(fakeSavedEvents)
      coEvery { eventPublishingService.storeAndPublishEvent(any(), any()) }.returns(Unit)

      underTest.saveDeathNotificationEvents(ingressEventData, dataDetail, dataProcessorMessage)

      coVerify(exactly = 1) {
        egressEventDataRepository.saveAll(withArg<Iterable<EgressEventData>> {
          assertThat(it).hasSize(2)
          val simpleEvent = it.find { event -> event.consumerSubscriptionId == simpleSubscription.id }
          val complexEvent = it.find { event -> event.consumerSubscriptionId == complexSubscription.id }

          assertThat(simpleEvent?.ingressEventId).isEqualTo(complexEvent?.ingressEventId)
            .isEqualTo(ingressEventData.eventId)
          assertThat(simpleEvent?.datasetId).isEqualTo(complexEvent?.datasetId)
            .isEqualTo(dataProcessorMessage.datasetId)
          assertThat(simpleEvent?.dataId).isEqualTo(complexEvent?.dataId)
            .isEqualTo(dataDetail.id)
          assertThat(simpleEvent?.whenCreated).isEqualTo(complexEvent?.whenCreated)
            .isEqualTo(dataProcessorMessage.eventTime)
          assertThat(simpleEvent?.dataExpiryTime).isEqualTo(complexEvent?.dataExpiryTime)
            .isEqualTo(dataProcessorMessage.eventTime.plusHours(1))

          assertThat(simpleEvent?.dataPayload)
            .isEqualTo(null)
          assertThat(complexEvent?.dataPayload)
            .isEqualTo(null)
        })
      }
      fakeSavedEvents.collect {
        coVerify(exactly = 1) { eventPublishingService.storeAndPublishEvent(it.id, dataProcessorMessage) }
      }
    }
  }

  @Test
  fun `saveDeathNotificationEvents throws error for not valid ID`() {
    runBlocking {
      val dataProcessorMessage = DataProcessorMessage(
        datasetId = "INVALID",
        eventTypeId = "DEATH_NOTIFICATION",
        eventTime = LocalDateTime.of(2010, 1, 1, 12, 0),
        publisher = "HMPO",
        storePayload = true,
        subscriptionId = UUID.randomUUID(),
        id = "123456789",
        details = null
      )
      val dataDetail = DataDetail(id = dataProcessorMessage.id!!, data = null)

      coEvery { consumerSubscriptionRepository.findAllByIngressEventType(ingressEventData.eventTypeId) }
        .returns(consumerSubscriptions)

      val exception = assertThrows<RuntimeException> {
        underTest.saveDeathNotificationEvents(ingressEventData, dataDetail, dataProcessorMessage)
      }

      assertThat(exception.message).isEqualTo("Unknown DataSet ${dataProcessorMessage.datasetId}")

      coVerify(exactly = 0) { egressEventDataRepository.saveAll(any<Iterable<EgressEventData>>()) }
      coVerify(exactly = 0) { eventPublishingService.storeAndPublishEvent(any(), any()) }
    }
  }

  private val ingressEventData = IngressEventData(
    eventTypeId = "DEATH_NOTIFICATION",
    subscriptionId = UUID.randomUUID(),
    datasetId = UUID.randomUUID().toString(),
    dataId = "HMPO",
    dataExpiryTime = LocalDateTime.now(),
    dataPayload = null
  )
  private val simpleSubscription = ConsumerSubscription(
    consumerId = UUID.randomUUID(),
    ingressEventType = "DEATH_NOTIFICATION",
    enrichmentFields = "firstName,lastName"
  )
  private val complexSubscription = ConsumerSubscription(
    consumerId = UUID.randomUUID(),
    ingressEventType = "DEATH_NOTIFICATION",
    enrichmentFields = "firstName,lastName,sex"
  )
  private val consumerSubscriptions = flowOf(simpleSubscription, complexSubscription)
  private val simpleDeathNotificationDetails = DeathNotificationDetails(
    firstName = "Alice",
    lastName = "Smith"
  )
  private val complexDeathNotificationDetails = simpleDeathNotificationDetails.copy(
    sex = "female"
  )
  private val fakeSavedEvents = flowOf(
    EgressEventData(
      consumerSubscriptionId = UUID.randomUUID(),
      ingressEventId = UUID.randomUUID(),
      datasetId = UUID.randomUUID().toString(),
      dataId = "HMPO",
      dataExpiryTime = LocalDateTime.now(),
      dataPayload = null
    ),
    EgressEventData(
      consumerSubscriptionId = UUID.randomUUID(),
      ingressEventId = UUID.randomUUID(),
      datasetId = UUID.randomUUID().toString(),
      dataId = "HMPO",
      dataExpiryTime = LocalDateTime.now(),
      dataPayload = null
    )
  )
}
