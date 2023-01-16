package uk.gov.gdx.datashare.helper

import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import uk.gov.gdx.datashare.config.PostgresContainer

@ActiveProfiles("test")
abstract class TestBase {

  companion object {
    private val pgContainer = PostgresContainer.instance

    @JvmStatic
    @DynamicPropertySource
    fun properties(registry: DynamicPropertyRegistry) {
      pgContainer?.run {
        registry.add("db.jdbc.uri", pgContainer::getJdbcUrl)
        registry.add("db.username", pgContainer::getUsername)
        registry.add("db.password", pgContainer::getPassword)
        registry.add("db.r2dbc.uri") { pgContainer.jdbcUrl.replace("jdbc:", "r2dbc:") }
      }
    }
  }
}
