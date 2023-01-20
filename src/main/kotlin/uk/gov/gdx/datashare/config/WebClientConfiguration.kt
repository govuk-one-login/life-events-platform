package uk.gov.gdx.datashare.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class WebClientConfiguration(
  @Value("\${api.base.url.lev}") private val levApiRootUri: String,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Bean
  fun levApiWebClient(): WebClient {
    val httpClient = HttpClient.create().responseTimeout(Duration.ofMinutes(2))
    return WebClient.builder()
      .baseUrl(levApiRootUri)
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .defaultHeader("X-Auth-Aud", "gdx-data-share")
      .defaultHeader("X-Auth-Username", "gdx-data-share-user")
      .build()
  }
}
