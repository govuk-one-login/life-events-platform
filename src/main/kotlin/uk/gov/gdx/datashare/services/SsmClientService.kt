package uk.gov.gdx.datashare.services

import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.ssm.SsmClient

@Service
class SsmClientService {
  @Bean
  fun ssmClient(): SsmClient {
    return SsmClient.create()
  }
}
