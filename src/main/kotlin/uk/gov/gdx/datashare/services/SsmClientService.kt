package uk.gov.gdx.datashare.services

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.services.ssm.SsmClient

@Configuration
class SsmClientService {
  @Bean
  fun ssmClient(): SsmClient {
    return SsmClient.create()
  }
}
