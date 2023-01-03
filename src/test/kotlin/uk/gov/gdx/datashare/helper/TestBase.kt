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
        registry.add("spring.flyway.url", pgContainer::getJdbcUrl)
        registry.add("spring.flyway.user", pgContainer::getUsername)
        registry.add("spring.flyway.password", pgContainer::getPassword)
        registry.add("spring.r2dbc.url") { pgContainer.jdbcUrl.replace("jdbc:", "r2dbc:") }
        registry.add("spring.r2dbc.username", pgContainer::getUsername)
        registry.add("spring.r2dbc.password", pgContainer::getPassword)
      }
    }
  }
}
