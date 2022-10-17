package uk.gov.gdx.datashare.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class WebClientConfiguration(
  @Value("\${api.base.url.oauth}") val auth0BaseUri: String,
  @Value("\${api.base.url.hmrc}") val hmrcApiRootUri: String,
  @Value("\${api.base.url.lev}") private val levApiRootUri: String
) {

  @Bean
  fun auth0WebClient(): WebClient {
    return WebClient.builder()
      .baseUrl(auth0BaseUri)
      .build()
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

  @Bean
  fun hmrcApiWebClient(): WebClient {
    val httpClient = HttpClient.create().responseTimeout(Duration.ofMinutes(2))
    return WebClient.builder()
      .baseUrl(hmrcApiRootUri)
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .build()
  }
}
