package uk.gov.gdx.datashare.config

import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.gdx.datashare.service.DbCredentialsService
import javax.sql.DataSource

@Configuration
class ShedlockConfiguration {
  @Bean
  fun dataSource(
    @Value("\${db.jdbc.uri}") url: String,
    @Value("\${spring.datasource.hikari.max-lifetime}") maxLifetime: Long,
    dbCredentialsService: DbCredentialsService,
  ): DataSource {
    return RdsIAMHikariDataSource(dbCredentialsService).also {
      it.username = dbCredentialsService.username()
      it.jdbcUrl = url
      it.maxLifetime = maxLifetime
    }
  }

  @Bean
  fun lockProvider(dataSource: DataSource): LockProvider {
    return JdbcTemplateLockProvider(
      JdbcTemplateLockProvider.Configuration.builder()
        .withJdbcTemplate(JdbcTemplate(dataSource))
        .usingDbTime()
        .build(),
    )
  }
}
