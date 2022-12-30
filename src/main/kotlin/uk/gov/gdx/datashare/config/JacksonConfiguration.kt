package uk.gov.gdx.datashare.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class JacksonConfiguration {
  @Bean
  @Primary
  fun objectMapper(): ObjectMapper = ObjectMapper().findAndRegisterModules()
}