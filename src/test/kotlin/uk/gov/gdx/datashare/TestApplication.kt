package uk.gov.gdx.datashare

import io.mockk.every
import io.mockk.mockk
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParameterResponse
import software.amazon.awssdk.services.ssm.model.Parameter

@SpringBootApplication
class TestApplication {
  @Bean
  @Primary
  fun mockSsmClient(): SsmClient {
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
