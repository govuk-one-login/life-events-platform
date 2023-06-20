package uk.gov.gdx.datashare.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.toedter.spring.hateoas.jsonapi.JsonApiConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.text.SimpleDateFormat

@Configuration
class JacksonConfiguration {
  @Bean
  @Primary
  fun objectMapper(): ObjectMapper {
    val objectMapper = ObjectMapper()
      .findAndRegisterModules()
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

    objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

    return objectMapper
  }

  @Bean
  @Primary
  fun jsonApiConfiguration(): JsonApiConfiguration =
    JsonApiConfiguration()
      .withObjectMapperCustomizer { ob ->
        ob.findAndRegisterModules()
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
          .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      }
      .withJsonApiCompliantLinks(false)
}
