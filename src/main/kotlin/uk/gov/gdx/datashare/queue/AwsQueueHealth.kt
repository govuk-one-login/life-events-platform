package uk.gov.gdx.datashare.queue

import com.amazonaws.services.sqs.model.GetQueueAttributesRequest
import com.amazonaws.services.sqs.model.GetQueueAttributesResult
import com.amazonaws.services.sqs.model.QueueAttributeName.All
import com.amazonaws.services.sqs.model.QueueAttributeName.ApproximateNumberOfMessages
import com.amazonaws.services.sqs.model.QueueAttributeName.ApproximateNumberOfMessagesNotVisible
import com.amazonaws.services.sqs.model.QueueAttributeName.RedrivePolicy
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.Health.Builder
import org.springframework.boot.actuate.health.HealthIndicator
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

class AwsQueueHealth(private val awsQueue: AwsQueue) : HealthIndicator {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  override fun health(): Health = buildHealth(checkQueueHealth(), checkDlqHealth())

  @JvmInline
  private value class HealthDetail(private val detail: Pair<String, String>) {
    fun key() = detail.first
    fun value() = detail.second
  }

  private fun checkQueueHealth(): List<Result<HealthDetail>> {
    val results = mutableListOf<Result<HealthDetail>>()
    results += success(HealthDetail("queueName" to awsQueue.queueName))

    getQueueAttributes().map { attributesResult ->
      results += success(HealthDetail("messagesOnQueue" to """${attributesResult.attributes[ApproximateNumberOfMessages.toString()]}"""))
      results += success(HealthDetail("messagesInFlight" to """${attributesResult.attributes[ApproximateNumberOfMessagesNotVisible.toString()]}"""))

      awsQueue.dlqName?.let {
        attributesResult.attributes["$RedrivePolicy"] ?: run { results += failure(MissingRedrivePolicyException(awsQueue.id)) }
      }
    }.onFailure { throwable -> results += failure(throwable) }

    return results.toList()
  }
  private fun checkDlqHealth(): List<Result<HealthDetail>> {
    val results = mutableListOf<Result<HealthDetail>>()
    awsQueue.dlqName?.run {
      results += success(HealthDetail("dlqName" to awsQueue.dlqName))

      awsQueue.sqsDlqClient?.run {
        getDlqAttributes().map { attributesResult ->
          results += success(
            HealthDetail(
              "messagesOnDlq" to
                """${attributesResult.attributes[ApproximateNumberOfMessages.toString()]}"""
            )
          )
        }.onFailure { throwable -> results += failure(throwable) }
      }
    }
    return results.toList()
  }

  private fun buildHealth(queueResults: List<Result<HealthDetail>>, dlqResults: List<Result<HealthDetail>>): Health {
    val healthBuilder = if (queueStatus(dlqResults, queueResults) == "UP") Builder().up() else Builder().down()
    queueResults.forEach { healthBuilder.addHealthResult(it) }

    if (dlqResults.isNotEmpty()) {
      healthBuilder.withDetail("dlqStatus", dlqStatus(dlqResults, queueResults))
      dlqResults.forEach { healthBuilder.addHealthResult(it) }
    }

    return healthBuilder.build()
  }

  private fun queueStatus(dlqResults: List<Result<HealthDetail>>, queueResults: List<Result<HealthDetail>>): String =
    if ((queueResults + dlqResults).any { it.isFailure }) "DOWN" else "UP"

  private fun dlqStatus(dlqResults: List<Result<HealthDetail>>, queueResults: List<Result<HealthDetail>>): String =
    if (queueResults.any(::isMissingRedrivePolicy).or(dlqResults.any { it.isFailure })) "DOWN" else "UP"

  private fun isMissingRedrivePolicy(result: Result<HealthDetail>) = result.exceptionOrNull() is MissingRedrivePolicyException

  private fun Builder.addHealthResult(result: Result<HealthDetail>) =
    result
      .onSuccess { healthDetail -> withDetail(healthDetail.key(), healthDetail.value()) }
      .onFailure { throwable ->
        withException(throwable)
          .also { log.error("Queue health for queueId ${awsQueue.id} failed due to exception", throwable) }
      }

  private fun getQueueAttributes(): Result<GetQueueAttributesResult> {
    return runCatching {
      awsQueue.sqsClient.getQueueAttributes(GetQueueAttributesRequest(awsQueue.queueUrl).withAttributeNames(All))
    }
  }

  private fun getDlqAttributes(): Result<GetQueueAttributesResult> =
    runCatching {
      awsQueue.sqsDlqClient?.getQueueAttributes(GetQueueAttributesRequest(awsQueue.dlqUrl).withAttributeNames(All))
        ?: throw MissingDlqClientException(awsQueue.dlqName)
    }
}

class MissingRedrivePolicyException(queueId: String) : RuntimeException("The main queue for $queueId is missing a $RedrivePolicy")
class MissingDlqClientException(dlqName: String?) : RuntimeException("Attempted to access dlqclient for $dlqName that does not exist")
