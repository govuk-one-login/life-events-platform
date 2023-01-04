package uk.gov.gdx.datashare.config

import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

@Configuration
class ShedlockConfiguration {
  @Bean
  fun dataSource(): DataSource {
    val dataSource = DataSourceBuilder.create()
    dataSource.url("jdbc:postgresql://datashare-db:5432/datashare?sslmode=prefer")
    dataSource.username("datashare")
    dataSource.password("datashare")
    return dataSource.build()
  }

  @Bean
  fun lockProvider(dataSource: DataSource): LockProvider {
    return JdbcTemplateLockProvider(
      JdbcTemplateLockProvider.Configuration.builder()
        .withJdbcTemplate(JdbcTemplate(dataSource))
        .usingDbTime()
        .build()
    )
  }
}