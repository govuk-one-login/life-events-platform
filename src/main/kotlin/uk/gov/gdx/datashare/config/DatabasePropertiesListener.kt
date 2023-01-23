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
      environment.getProperty("spring.datasource.username")
        ?: throw IllegalStateException("No db username specified"),
      environment.getProperty<Int>("db.port"),
      environment.getProperty("db.hostname"),
      environment.getProperty("spring.datasource.password"),
      environment.getProperty("db.rds-region"),
    )

    Properties().also { it.setProperty("spring.datasource.password", dbCredentialsService.password()) }
      .also { environment.propertySources.addFirst(PropertiesPropertySource("databaseProps", it)) }
  }
}
