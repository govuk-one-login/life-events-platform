package uk.gov.gdx.datashare.config

import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

@Configuration
class ShedlockConfiguration {
  @Bean
  fun dataSource(
    @Value("\${spring.flyway.url}") url: String,
    @Value("\${spring.flyway.user}") username: String,
    @Value("\${spring.flyway.password}") password: String,
  ): DataSource {
    return DataSourceBuilder.create().url(url).username(username).password(password).build()
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