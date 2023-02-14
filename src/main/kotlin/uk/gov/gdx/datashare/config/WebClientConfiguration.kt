package uk.gov.gdx.datashare.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.oauth2.client.*
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

@Configuration
class WebClientConfiguration(
  @Value("\${api.base.url.lev}") private val levApiRootUri: String,
  @Value("\${api.base.url.prisoner-search:-}") private val prisonerSearchApiUri: String,
  @Value("\${api.base.lev.api.client.name:-}") private val levApiClientName: String,
  @Value("\${api.base.lev.api.client.user:-}") private val levApiClientUser: String,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
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

  @Bean
  @ConditionalOnProperty(name = ["api.base.prisoner-event.enabled"], havingValue = "true")
  fun prisonerSearchApiWebClient(
    builder: Builder,
    prisonerSearchAuthorizedClientManager: OAuth2AuthorizedClientManager,
  ): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(prisonerSearchAuthorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId("prisoner-search")

    return builder
      .baseUrl(prisonerSearchApiUri)
      .clientConnector(ReactorClientHttpConnector(createHttpClient("prisonerSearch")))
      .filter(oauth2Client)
      .codecs { codecs -> codecs.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) }
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

  @Bean
  @ConditionalOnProperty(name = ["api.base.prisoner-event.enabled"], havingValue = "true")
  fun offenderSearchHealthWebClient(builder: Builder): WebClient {
    return builder
      .baseUrl(prisonerSearchApiUri)
      .build()
  }

  @Bean
  @ConditionalOnProperty(name = ["api.base.prisoner-event.enabled"], havingValue = "true")
  fun prisonerSearchAuthorizedClientManager(
    clientRegistrationRepository: ClientRegistrationRepository?,
    oAuth2AuthorizedClientService: OAuth2AuthorizedClientService?,
  ): OAuth2AuthorizedClientManager? {
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build()
    val authorizedClientManager =
      AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientService)
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
    return authorizedClientManager
  }
}
