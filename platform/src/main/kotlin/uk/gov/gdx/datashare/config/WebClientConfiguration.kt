package uk.gov.gdx.datashare.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

@Configuration
class WebClientConfiguration(
  @Value("\${api.base.url.lev}") private val levApiRootUri: String,
  @Value("\${api.base.lev.api.client.name}") private val levApiClientName: String,
  @Value("\${api.base.lev.api.client.user}") private val levApiClientUser: String,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Bean
  fun setupHttpClient(): WebClient {
    return WebClient.builder()
      .clientConnector(ReactorClientHttpConnector(createHttpClient("setupHttpClient")))
      .build()
  }

  @Bean
  fun levApiWebClient(builder: Builder): WebClient {
    return builder
      .baseUrl(levApiRootUri)
      .clientConnector(ReactorClientHttpConnector(createHttpClient("levApi")))
      .defaultHeader("X-Auth-Aud", levApiClientName)
      .defaultHeader("X-Auth-Username", levApiClientUser)
      .build()
  }

  private fun createHttpClient(connectionName: String): HttpClient {
    val connectionProvider = ConnectionProvider.builder(connectionName)
      .maxConnections(100)
      .maxIdleTime(Duration.ofSeconds(20))
      .maxLifeTime(Duration.ofSeconds(60))
      .pendingAcquireTimeout(Duration.ofSeconds(60))
      .evictInBackground(Duration.ofSeconds(120))
      .build()

    return HttpClient.create(connectionProvider).responseTimeout(Duration.ofSeconds(10))
  }
}
