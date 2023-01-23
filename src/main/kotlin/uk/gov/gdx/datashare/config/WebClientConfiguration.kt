package uk.gov.gdx.datashare.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.oauth2.client.*
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class WebClientConfiguration(
  @Value("\${api.base.url.lev}") private val levApiRootUri: String,
  @Value("\${api.base.url.data-receiver}") private val dataReceiverUri: String,
  @Value("\${api.base.url.event-data-retrieval}") private val eventDataRetrievalUri: String,
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

  @Bean
  fun dataReceiverApiWebClient(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId("data-receiver")

    val httpClient = HttpClient.create().responseTimeout(Duration.ofMinutes(2))
    return WebClient.builder()
      .baseUrl(dataReceiverUri)
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .filter(oauth2Client)
      .build()
  }

  @Bean
  fun eventDataRetrievalApiWebClient(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId("event-data-retrieval")

    val httpClient = HttpClient.create().responseTimeout(Duration.ofMinutes(2))
    return WebClient.builder()
      .baseUrl(eventDataRetrievalUri)
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .filter(oauth2Client)
      .build()
  }

  @Bean
  fun authorizedClientManager(
    clientRegistrationRepository: ClientRegistrationRepository,
    oAuth2AuthorizedClientService: OAuth2AuthorizedClientService,
  ): OAuth2AuthorizedClientManager {
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
      .clientCredentials()
      .build()

    val authorizedClientManager = AuthorizedClientServiceOAuth2AuthorizedClientManager(
      clientRegistrationRepository,
      oAuth2AuthorizedClientService,
    )
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
    return authorizedClientManager
  }
}
