package uk.gov.gdx.datashare.config

import com.zaxxer.hikari.HikariDataSource
import org.javers.core.Javers
import org.javers.core.JaversBuilder
import org.javers.repository.sql.ConnectionProvider
import org.javers.repository.sql.DialectName
import org.javers.repository.sql.SqlRepositoryBuilder
import org.javers.spring.auditable.AuthorProvider
import org.javers.spring.auditable.EmptyPropertiesProvider
import org.javers.spring.auditable.SpringSecurityAuthorProvider
import org.javers.spring.auditable.aspect.springdata.JaversSpringDataAuditableRepositoryAspect
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DataSourceUtils

@Configuration
class JaversConfiguration {
  @Bean
  fun javers(dataSource: HikariDataSource): Javers {
    val connectionProvider = ConnectionProvider {
      DataSourceUtils.getConnection(dataSource)
    }

    val repository = SqlRepositoryBuilder.sqlRepository().withConnectionProvider(connectionProvider)
      .withDialect(DialectName.POSTGRES).build()

    return JaversBuilder
      .javers()
      .registerJaversRepository(repository)
      .build()
  }

  @Bean
  fun authorProvider(): AuthorProvider? {
    return SpringSecurityAuthorProvider()
  }

  @Bean
  fun javersSpringDataAuditableAspect(javers: Javers): JaversSpringDataAuditableRepositoryAspect? {
    val commitPropertiesProvider = EmptyPropertiesProvider()
    return JaversSpringDataAuditableRepositoryAspect(
      javers,
      authorProvider(),
      commitPropertiesProvider,
    )
  }
}
