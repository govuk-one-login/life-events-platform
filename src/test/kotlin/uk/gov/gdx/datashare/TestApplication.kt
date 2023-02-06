package uk.gov.gdx.datashare

import io.mockk.every
import io.mockk.mockk
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Primary
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParameterResponse
import software.amazon.awssdk.services.ssm.model.Parameter
import uk.gov.gdx.datashare.services.SsmClientService

@SpringBootApplication
@ComponentScan(excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = [GdxDataSharePoc::class, SsmClientService::class])])
class TestApplication {
  @Bean
  @Primary
  fun ssmClient(): SsmClient {
    val mockSsmClient = mockk<SsmClient>()
    val parameter = Parameter.builder().value("mock_parameter").build()
    val getParameterResponse = GetParameterResponse.builder().parameter(parameter).build()
    every { mockSsmClient.getParameter(any<software.amazon.awssdk.services.ssm.model.GetParameterRequest>()) }.returns(
      getParameterResponse,
    )
    return mockSsmClient
  }
}

fun main(args: Array<String>) {
  runApplication<TestApplication>(*args)
}
