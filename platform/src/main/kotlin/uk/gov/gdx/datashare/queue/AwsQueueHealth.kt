package uk.gov.gdx.datashare.queue

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.Health.Builder
import org.springframework.boot.actuate.health.HealthIndicator
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesResponse
import software.amazon.awssdk.services.sqs.model.QueueAttributeName
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
      results += success(HealthDetail("messagesOnQueue" to """${attributesResult.attributes()[QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES]}"""))
      results += success(HealthDetail("messagesInFlight" to """${attributesResult.attributes()[QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE]}"""))

      awsQueue.dlqName?.let {
        attributesResult.attributes()[QueueAttributeName.REDRIVE_POLICY] ?: run { results += failure(MissingRedrivePolicyException(awsQueue.id)) }
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
                """${attributesResult.attributes()[QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES]}""",
            ),
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

  private fun getQueueAttributes(): Result<GetQueueAttributesResponse> {
    return runCatching {
      awsQueue.sqsClient.getQueueAttributes(GetQueueAttributesRequest.builder().queueUrl(awsQueue.queueUrl).attributeNames(QueueAttributeName.ALL).build())
    }
  }

  private fun getDlqAttributes(): Result<GetQueueAttributesResponse> =
    runCatching {
      awsQueue.sqsDlqClient?.getQueueAttributes(GetQueueAttributesRequest.builder().queueUrl(awsQueue.dlqUrl).attributeNames(QueueAttributeName.ALL).build())
        ?: throw MissingDlqClientException(awsQueue.dlqName)
    }
}

class MissingRedrivePolicyException(queueId: String) : RuntimeException("The main queue for $queueId is missing a ${QueueAttributeName.REDRIVE_POLICY}")
class MissingDlqClientException(dlqName: String?) : RuntimeException("Attempted to access dlqclient for $dlqName that does not exist")
