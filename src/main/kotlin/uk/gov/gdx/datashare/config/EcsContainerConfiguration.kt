package uk.gov.gdx.datashare.config

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class EcsContainerConfiguration(
  @Value("\${ecs.container.metadata.uri.v4:-}") private val metadataUri: String?,
) {
  @Bean
  fun taskId(baseHttpClient: WebClient): String {
    if (metadataUri == null) {
      return "local"
    }
    return runBlocking {
      baseHttpClient
        .get()
        .uri(metadataUri)
        .retrieve()
        .bodyToMono(EcsContainerMetadata::class.java)
        .block()
        ?.labels
        ?.taskArn
        ?: "local"
    }
  }
}

data class EcsContainerMetadata(
  @JsonProperty("Labels")
  val labels: EcsContainerMetadataLabels,
)

data class EcsContainerMetadataLabels(
  @JsonProperty("com.amazonaws.ecs.task-arn")
  val taskArn: String,
)
