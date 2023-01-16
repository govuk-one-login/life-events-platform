package uk.gov.gdx.datashare.config

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent
import org.springframework.context.ApplicationListener
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.env.getProperty
import uk.gov.gdx.datashare.service.DbCredentialsService
import java.util.*

class DatabasePropertiesListener : ApplicationListener<ApplicationEnvironmentPreparedEvent> {
  override fun onApplicationEvent(event: ApplicationEnvironmentPreparedEvent) {
    val environment: ConfigurableEnvironment = event.environment
    val dbCredentialsService = DbCredentialsService(
      environment.getProperty<DbCredentialsService.DbProvider>("db.provider")
        ?: throw IllegalStateException("No db provider specified"),
      environment.getProperty("db.hostname")
        ?: throw IllegalStateException("No db host specified"),
      environment.getProperty<Int>("db.port")
        ?: throw IllegalStateException("No db port specified"),
      environment.getProperty("db.username")
        ?: throw IllegalStateException("No db username specified"),
      environment.getProperty("db.password"),
      environment.getProperty("db.rds-region"),
    )

    Properties().also { it.setProperty("spring.flyway.password", dbCredentialsService.password()) }
      .also { environment.propertySources.addFirst(PropertiesPropertySource("databaseProps", it)) }
  }
}
