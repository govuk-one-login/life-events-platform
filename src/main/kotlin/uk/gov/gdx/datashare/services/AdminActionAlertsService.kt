package uk.gov.gdx.datashare.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishRequest

@Service
class AdminActionAlertsService(
  @Value("\${alert-sns-topic-arn:#{null}}") private val topicArn: String?,
  @Value("\${environment}") private val environment: String,
  private val objectMapper: ObjectMapper,
) {
  private val snsClient by lazy { SnsClient.create() }

  fun noticeAction(action: AdminAction) {
    topicArn ?: return
    val request = PublishRequest.builder()
      .message(actionToMessage(action))
      .topicArn(topicArn)
      .build()

    snsClient.publish(request)
  }

  private fun actionToMessage(action: AdminAction): String {
    return "${currentPrincipal() ?: "unknown"} performed ${action.name}, environment: $environment, details: ${action.details.toJson()}"
  }

  private fun currentPrincipal(): String? {
    val authentication = SecurityContextHolder.getContext().authentication
    if (authentication !is AnonymousAuthenticationToken) {
      return authentication.name
    }
    return null
  }

  private fun Any.toJson() = objectMapper.writeValueAsString(this)
}

data class AdminAction(val name: String, val details: Any)
