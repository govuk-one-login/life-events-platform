package uk.gov.gdx.datashare.helpers

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
        registry.add("spring.datasource.url") { datasourceUrl() }
        registry.add("spring.datasource.username", pgContainer::getUsername)
        registry.add("spring.datasource.password", pgContainer::getPassword)
      }
    }

    private fun datasourceUrl(): String? {
      return pgContainer?.jdbcUrl?.split(':')?.toMutableList()?.let {
        it.add(1, "aws-wrapper")
        it.joinToString(":")
      }
    }
  }
}
