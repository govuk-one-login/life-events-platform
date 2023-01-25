package uk.gov.gdx.datashare.queue

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "sqs")
data class SqsProperties(
  val provider: String = "aws",
  val region: String = "eu-west-2",
  val localstackUrl: String = "http://localhost:4566",
  val queues: Map<String, QueueConfig> = mapOf(),
) {
  data class QueueConfig(
    val queueName: String,
    val subscribeTopicId: String = "",
    val subscribeFilter: String = "",
    val dlqName: String = "",
    val dlqMaxReceiveCount: Int = 5,
  )

  init {
    queues.forEach { (queueId, queueConfig) ->
      queueIdMustBeLowerCase(queueId)
      queueNamesMustExist(queueId, queueConfig)
    }
    checkForAwsDuplicateValues()
    checkForLocalStackDuplicateValues()
  }

  private fun queueIdMustBeLowerCase(queueId: String) {
    if (queueId != queueId.lowercase()) throw InvalidAwsSqsPropertiesException("queueId $queueId is not lowercase")
  }

  private fun queueNamesMustExist(queueId: String, queueConfig: QueueConfig) {
    if (queueConfig.queueName.isEmpty()) throw InvalidAwsSqsPropertiesException("queueId $queueId does not have a queue name")
  }

  private fun checkForAwsDuplicateValues() {
    if (provider == "aws") {
      mustNotContainDuplicates("queue names", queues) { it.value.queueName }
      mustNotContainDuplicates("dlq names", queues) { it.value.dlqName }
    }
  }

  private fun checkForLocalStackDuplicateValues() {
    if (provider == "localstack") {
      mustNotContainDuplicates("queue names", queues) { it.value.queueName }
      mustNotContainDuplicates("dlq names", queues) { it.value.dlqName }
    }
  }

  private fun <T> mustNotContainDuplicates(description: String, source: Map<String, T>, valueFinder: (Map.Entry<String, T>) -> String) {
    val duplicateValues = source.mapValues(valueFinder).values.filter { it.isNotEmpty() }.groupingBy { it }.eachCount().filterValues { it > 1 }
    if (duplicateValues.isNotEmpty()) {
      throw InvalidAwsSqsPropertiesException("Found duplicated $description: ${duplicateValues.keys}")
    }
  }
}

class InvalidAwsSqsPropertiesException(message: String) : IllegalStateException(message)
