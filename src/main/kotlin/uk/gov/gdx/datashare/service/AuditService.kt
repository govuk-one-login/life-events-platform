package uk.gov.gdx.datashare.service

import com.amazonaws.services.sqs.model.SendMessageRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.time.Instant

@Service
class AuditService(
  @Value("\${spring.application.name}")
  private val serviceName: String,
  private val hmppsQueueService: HmppsQueueService,
  private val objectMapper: ObjectMapper,
) {
  private val auditQueue by lazy { hmppsQueueService.findByQueueId("audit") as HmppsQueue }
  private val auditSqsClient by lazy { auditQueue.sqsClient }
  private val auditQueueUrl by lazy { auditQueue.queueUrl }

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun sendMessage(auditType: AuditType, id: String, details: Any, username: String = "annon") {

    val auditEvent = AuditEvent(
      what = auditType.name,
      who = username,
      service = serviceName,
      details = objectMapper.writeValueAsString(details)
    )
    log.debug("Audit {} ", auditEvent)

    auditSqsClient.sendMessage(
      SendMessageRequest(
        auditQueueUrl,
        auditEvent.toJson()
      )
    )
  }

  private fun Any.toJson() = objectMapper.writeValueAsString(this)
}

data class AuditEvent(
  val what: String,
  val `when`: Instant = Instant.now(),
  val who: String,
  val service: String,
  val details: String? = null,
)
enum class AuditType {
  EVENT_OCCURRED,
  DATA_SHARE_EVENT_PUBLISHED,
  CLIENT_CONSUMED_EVENT
}
