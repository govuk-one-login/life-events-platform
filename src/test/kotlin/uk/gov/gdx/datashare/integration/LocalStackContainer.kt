package uk.gov.gdx.datashare.integration

import org.slf4j.LoggerFactory
import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import java.io.IOException
import java.net.ServerSocket

object LocalStackContainer {
  private val log = LoggerFactory.getLogger(this::class.java)
  val instance by lazy { startLocalStackIfNotRunning() }

  fun setLocalStackProperties(localStackContainer: LocalStackContainer, registry: DynamicPropertyRegistry) {
    val localstackUrl = localStackContainer.getEndpointOverride(LocalStackContainer.Service.SNS).toString()
    val region = localStackContainer.region
    registry.add("sqs.localstackUrl") { localstackUrl }
    registry.add("sqs.region") { region }
  }

  private fun startLocalStackIfNotRunning(): LocalStackContainer? {
    if (localstackIsRunning()) return null
    val logConsumer = Slf4jLogConsumer(log).withPrefix("localstack")
    return LocalStackContainer(
      DockerImageName.parse("localstack/localstack").withTag("1.2"),
    ).apply {
      withServices(LocalStackContainer.Service.SQS, LocalStackContainer.Service.SNS, LocalStackContainer.Service.S3)
      withEnv("DEFAULT_REGION", "eu-west-2")
      waitingFor(
        Wait.forLogMessage(".*Running on.*", 1),
      )
      start()
      followOutput(logConsumer)
    }
  }

  private fun localstackIsRunning(): Boolean =
    try {
      val serverSocket = ServerSocket(4566)
      serverSocket.localPort == 0
    } catch (e: IOException) {
      true
    }
}
