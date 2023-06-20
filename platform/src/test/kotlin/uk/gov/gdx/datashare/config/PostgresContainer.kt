package uk.gov.gdx.datashare.config

import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.api.model.Ports
import org.slf4j.LoggerFactory
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.IOException
import java.net.ServerSocket

object PostgresContainer {
  val instance: PostgreSQLContainer<Nothing>? by lazy { startPostgresqlContainer() }

  private fun startPostgresqlContainer(): PostgreSQLContainer<Nothing>? {
    if (isPostgresRunning()) {
      log.warn("Using existing Postgres database")
      return null
    }
    log.info("Creating a Postgres database")
    return PostgreSQLContainer<Nothing>("postgres:14.6").apply {
      withEnv("HOSTNAME_EXTERNAL", "localhost")
      withStartupAttempts(5)
      withDatabaseName("datashareint_db")
      withUsername("test")
      withPassword("test")
      setWaitStrategy(Wait.forListeningPort())
      withExposedPorts()
      withUrlParam("wrapperPlugins", "")
      withCreateContainerCmdModifier { cmd -> cmd.withHostConfig(HostConfig().withPortBindings(PortBinding(Ports.Binding.bindPort(5434), ExposedPort(5432)))) }
      withReuse(true)

      start()
    }
  }

  private fun isPostgresRunning(): Boolean =
    try {
      val serverSocket = ServerSocket(5434)
      serverSocket.localPort == 0
    } catch (e: IOException) {
      true
    }

  private val log = LoggerFactory.getLogger(this::class.java)
}
