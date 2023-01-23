package uk.gov.gdx.datashare.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import uk.gov.gdx.datashare.service.DbCredentialsService
import javax.sql.DataSource

class RdsIAMHikariDataSource(private val dbCredentialsService: DbCredentialsService) : HikariDataSource() {
  override fun getPassword() = dbCredentialsService.password()
}

@Bean
@ConfigurationProperties(prefix = "spring.datasource")
fun dataSource(
  @Value("\${spring.datasource.url}") url: String,
  @Value("\${spring.datasource.hikari.max-lifetime}") maxLifetime: Long,
  dbCredentialsService: DbCredentialsService,
): DataSource {
  return RdsIAMHikariDataSource(dbCredentialsService).also {
    it.username = dbCredentialsService.username()
    it.jdbcUrl = url
    it.maxLifetime = maxLifetime
  }
}
