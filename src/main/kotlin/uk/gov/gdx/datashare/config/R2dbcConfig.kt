package uk.gov.gdx.datashare.config

import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.*
import org.reactivestreams.Publisher
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import reactor.core.publisher.Mono
import uk.gov.gdx.datashare.service.DbCredentialsService
import java.time.Duration

// Based on https://github.com/pgjdbc/r2dbc-postgresql/issues/529
@Configuration
class R2dbcConfig(
  val dbCredentialsService: DbCredentialsService,
  @Value("\${db.r2dbc.uri}") val dbUri: String,
  @Value("\${db.ssl-required}") val dbSslRequired: Boolean,
) : AbstractR2dbcConfiguration() {

  override fun connectionFactory(): ConnectionFactory {
    val options = ConnectionFactoryOptions.parse(dbUri)
    val stubOptions = options.mutate()
      .option(
        ConnectionFactoryOptions.USER,
        "placeholder",
      ).option(
        ConnectionFactoryOptions.PASSWORD,
        "placeholder",
      ).build()

    val connectionFactoryStub = ConnectionFactories.get(stubOptions)

    val connectionPublisher: Mono<out Connection?> = Mono.defer {
      val optionsToUse = options.mutate().option(
        ConnectionFactoryOptions.USER,
        dbCredentialsService.username(),
      )
        .option(
          ConnectionFactoryOptions.PASSWORD,
          dbCredentialsService.password(),
        )
        .option(
          ConnectionFactoryOptions.SSL,
          dbSslRequired,
        )
        .build()
      Mono.from(ConnectionFactories.get(optionsToUse).create())
    }

    return object : ConnectionFactory {
      override fun create(): Publisher<out Connection?> {
        return connectionPublisher
      }

      override fun getMetadata(): ConnectionFactoryMetadata {
        return connectionFactoryStub.metadata
      }
    }
  }

  @Bean
  @Primary
  fun connectionPool(): ConnectionPool {
    val poolConfiguration: ConnectionPoolConfiguration =
      ConnectionPoolConfiguration.builder()
        .connectionFactory(connectionFactory())
        .maxLifeTime(Duration.ofMinutes(14)) // IAM tokens are valid for 15 minutes
        .build()
    return ConnectionPool(poolConfiguration)
  }
}
