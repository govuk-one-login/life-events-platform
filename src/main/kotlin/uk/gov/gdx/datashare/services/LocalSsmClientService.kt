package uk.gov.gdx.datashare.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(name = ["ssm-client.enabled"], havingValue = "false")
class LocalSsmClientService(
  @Value("\${environment:-}") private val environment: String,
  private val mockSsmClientProperties: MockSsmClientProperties,
) : SsmClientService(environment) {

  override fun getParameter(parameterName: String): String {
    return mockSsmClientProperties.mockParameters[parameterName] ?: ""
  }
}

@ConstructorBinding
@ConfigurationProperties(prefix = "ssm-client")
data class MockSsmClientProperties(
  val mockParameters: Map<String, String> = mapOf(),
)
