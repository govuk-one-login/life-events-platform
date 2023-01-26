package uk.gov.gdx.datashare.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import uk.gov.gdx.datashare.repositories.EventApiAudit

@Configuration
class JdbcConfiguration(
  private val objectMapper: ObjectMapper,
) : AbstractJdbcConfiguration() {
  override fun userConverters(): List<*> {
    return listOf(
      EventApiAudit.EntityWritingConverter(objectMapper),
      EventApiAudit.EntityReadingConverter(objectMapper),
    )
  }
}
