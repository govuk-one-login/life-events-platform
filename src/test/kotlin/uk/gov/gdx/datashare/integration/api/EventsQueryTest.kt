package uk.gov.gdx.datashare.integration.api

import org.approvaltests.JsonApprovals
import org.approvaltests.core.Options
import org.approvaltests.scrubbers.Scrubbers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.repositories.AcquirerSubscriptionRepository
import uk.gov.gdx.datashare.repositories.EventData
import uk.gov.gdx.datashare.repositories.EventDataRepository
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.integration.MockIntegrationTestBase
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.integration.wiremock.mockLevApi
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.integration.wiremock.stubLevApiDeath
import java.time.LocalDateTime
import java.util.*

class EventsQueryTest : MockIntegrationTestBase() {
  companion object {
    private const val DWP_EVENT_RECEIVER = "dwp-event-receiver"
    private val eventId: UUID = UUID.fromString("2d32e854-75d4-4107-a501-167d41d6aef6")
    private val approvalsOptions = Options(Scrubbers::scrubGuid)

    @JvmStatic
    @RegisterExtension
    private var mockLevApi = mockLevApi()
  }

  @Autowired
  lateinit var mvc: MockMvc

  @Autowired
  private lateinit var eventDataRepository: EventDataRepository

  @Autowired
  private lateinit var acquirerSubscriptionRepository: AcquirerSubscriptionRepository

  @Test
  fun `getEvents returns death notification events in the correct format`() {
    // given
    thereIsAnEvent()

    // when
    val response = getEvents()

    // then
    JsonApprovals.verifyJson(response, approvalsOptions)
  }

  @Test
  fun `getEvents returns correctly formatted pagination links`() {
    // given
    thereAreFiveEvents()

    // when
    val response = getMiddlePageOfEvents()

    // then
    JsonApprovals.verifyJson(response, approvalsOptions)
  }

  @Test
  fun `getEvent returns a death notification event in the correct format`() {
    // given
    thereIsAnEvent()
    stubLevApiDeath(mockLevApi)

    // when
    val response = getEvent()

    // then
    JsonApprovals.verifyJson(response, approvalsOptions)
  }

  private fun getEvents(): String? = getEndpoint("/events")

  private fun getMiddlePageOfEvents(): String? = getEndpoint("/events?page[size]=1&page[number]=2")

  private fun getEvent(): String? = getEndpoint("/events/$eventId")

  private fun getEndpoint(endpoint: String): String? =
    mvc.get(endpoint) {
      headers(setAuthorisation(DWP_EVENT_RECEIVER, listOf(""), listOf("events/consume")))
    }.andReturn().response.contentAsString

  private fun thereIsAnEvent() {
    val acquirerSubscription = acquirerSubscriptionRepository.findAllByOauthClientIdAndEventTypeIsIn(
      DWP_EVENT_RECEIVER,
      listOf(EventType.DEATH_NOTIFICATION),
    ).first()
    val eventData = EventData(
      eventId,
      acquirerSubscription.acquirerSubscriptionId,
      "1234",
      LocalDateTime.of(2000, 5, 3, 12, 4, 3),
      LocalDateTime.of(2000, 5, 3, 12, 4, 3),
    )
    eventDataRepository.save(eventData)
  }

  private fun thereAreFiveEvents() {
    val acquirerSubscription = acquirerSubscriptionRepository.findAllByOauthClientIdAndEventTypeIsIn(
      DWP_EVENT_RECEIVER,
      listOf(EventType.DEATH_NOTIFICATION),
    ).first()
    listOf("1", "2", "3", "4", "5").map { sourceId ->

      val eventData = EventData(
        UUID.randomUUID(),
        acquirerSubscription.acquirerSubscriptionId,
        sourceId,
        LocalDateTime.of(2000, 5, 3, 12, 4, 3 + sourceId.toInt()),
        LocalDateTime.of(2000, 5, 3, 12, 4, 0 + sourceId.toInt()),
      )
      eventDataRepository.save(eventData)
    }
  }
}
