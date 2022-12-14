package uk.gov.gdx.datashare.service

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import uk.gov.gdx.datashare.repository.DataConsumerRepository
import uk.gov.gdx.datashare.resource.EventInformation
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.ConnectException
import java.time.LocalDateTime

@Service
class LegacyAdaptorOutbound(
  private val mapper: ObjectMapper,
  private val eventDataRetrievalApiWebClient: WebClient,
  private val auditService: AuditService,
  private val dataConsumerRepository: DataConsumerRepository
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @JmsListener(destination = "adaptor", containerFactory = "hmppsQueueContainerFactoryProxy")
  fun onPublishedEvent(message: String) = runBlocking {
    val (message, messageAttributes) = mapper.readValue(message, EventTopicMessage::class.java)
    val eventType = messageAttributes.eventType.Value
    log.info("Received message $message, type $eventType")

    val event = mapper.readValue(message, EventMessage::class.java)
    when (eventType) {
      "DEATH_NOTIFICATION" -> processDeathEvent(event)
      else -> {
        log.debug("Ignoring message with type $eventType")
      }
    }
  }

  suspend fun processDeathEvent(event: EventMessage) {
    log.debug("processing {}", event)

    // Go and get data from Event Retrieval API
    val deathData = getEventPayload(event.id)

    // turn into file and send to FTP
    val details = deathData.details
    if (details != null) {

      // who needs it?
      val findAllByLegacyFtp = dataConsumerRepository.findAllByLegacyFtp(true)
      findAllByLegacyFtp.collect {

        log.debug("Sending event via FTP to ${it.clientName}")
        val testFtpClient = FTPClient()
        val host = "localhost"
        val port = 31000
        try {
          testFtpClient.connect(host, port)
          testFtpClient.login("user", "password")

          val filename = "${event.id}.csv"
          withContext(Dispatchers.IO) {
            FileOutputStream(filename).apply { writeCsv(details) }
            testFtpClient.setFileType(FTP.ASCII_FILE_TYPE)
            testFtpClient.storeFile("/outbound/$filename", FileInputStream(filename))

            log.debug("File $filename sent to FTP Server")
          }
          testFtpClient.logout()

          auditService.sendMessage(
            auditType = AuditType.FTP_OUTBOUND,
            id = event.id,
            details = "FTP to ${it.clientName} : ${event.description}",
            username = it.clientId
          )
        } catch (e: ConnectException) {
          LegacyAdaptorInbound.log.warn("Failed to connect to FTP server")
        }
      }
    }
  }

  fun OutputStream.writeCsv(deathData: DeathNotification) {
    val writer = bufferedWriter()
    writer.write(""""Surname", "Firstnames", "Date of Birth", "Date of Birth", "Date of Death", "NINO"""")
    writer.newLine()
    writer.write("${deathData.deathDetails.surname}, ${deathData.deathDetails.forenames}, \"${deathData.deathDetails.dateOfBirth}\",\"${deathData.deathDetails.dateOfBirth}\",\"${deathData.deathDetails.dateOfDeath}\",\"${deathData.additionalInformation?.nino}\"")
    writer.newLine()
    writer.flush()
  }

  suspend fun getEventPayload(id: String): EventInformation =
    eventDataRetrievalApiWebClient.get()
      .uri("/event-data-retrieval/$id")
      .retrieve()
      .awaitBody()
}

data class GovEventType(val Value: String, val Type: String)
data class MessageAttributes(val eventType: GovEventType)
data class EventTopicMessage(
  val Message: String,
  val MessageAttributes: MessageAttributes
)

data class EventMessage(
  val id: String,
  val occurredAt: LocalDateTime,
  val description: String
)
