package uk.gov.gdx.datashare.config

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.TransactionManager

@Configuration
class TransactionManagerConfig {
  @Bean
  @Primary
  fun getTransactionManager(connectionFactory: ConnectionFactory): TransactionManager = R2dbcTransactionManager(connectionFactory)
}
