package uk.gov.gdx.datashare.service

import kotlinx.coroutines.runBlocking
import org.apache.commons.net.ftp.FTPClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import uk.gov.gdx.datashare.resource.EventToPublish
import uk.gov.gdx.datashare.resource.EventType
import java.io.File
import java.io.FileOutputStream
import java.net.ConnectException
import java.time.LocalDateTime

@Service
@ConditionalOnExpression("false")
class LegacyAdaptorInbound(
  private val dataReceiverApiWebClient: WebClient
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Scheduled(fixedRate = 1000 * 60)
  fun pollFtpServer() {
    val testFtpClient = FTPClient()
    val host = "localhost"
    val port = 31000
    try {
      testFtpClient.connect(host, port)
      testFtpClient.login("user", "password")

      listFiles(ftpClient = testFtpClient).forEach {
        log.debug("Retrieving file ${it.first}")
        val fileHandle = FileOutputStream(it.first)
        testFtpClient.retrieveFile(it.first, fileHandle)
        val file = File(it.first)
        file.forEachLine {
          runBlocking {
            postDataToReceiver(EventToPublish(eventType = EventType.DEATH_NOTIFICATION, eventDetails = it))
          }
        }
        val newLocation = "/archive/${it.first}"
        testFtpClient.rename(it.first, newLocation)
        file.delete()
      }

      testFtpClient.logout()
    } catch (e: ConnectException) {
      log.warn("Failed to connect to FTP server")
    } catch (e: RuntimeException) {
      log.error("Failed process line", e)
    }
  }

  suspend fun postDataToReceiver(payload: EventToPublish) =
    dataReceiverApiWebClient.post()
      .uri("/event-data-receiver")
      .bodyValue(payload)
      .retrieve()
      .awaitBodilessEntity()
}

private fun listFiles(ftpClient: FTPClient, path: String? = null): List<Pair<String, LocalDateTime>> {
  return ftpClient.listFiles(path)
    .map { fp -> Pair(fp.name, LocalDateTime.ofInstant(fp.timestamp.toInstant(), fp.timestamp.timeZone.toZoneId())) }
    .sortedBy { it.second }
    .toList()
}
