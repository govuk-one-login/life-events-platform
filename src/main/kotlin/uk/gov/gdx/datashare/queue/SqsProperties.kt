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
  val topics: Map<String, TopicConfig> = mapOf(),
) {
  data class QueueConfig(
    val queueName: String,
    val queueAccessKeyId: String = "",
    val queueSecretAccessKey: String = "",
    val subscribeTopicId: String = "",
    val subscribeFilter: String = "",
    val dlqName: String = "",
    val dlqAccessKeyId: String = "",
    val dlqSecretAccessKey: String = "",
    val dlqMaxReceiveCount: Int = 5,
  )

  data class TopicConfig(
    val arn: String = "",
    val accessKeyId: String = "",
    val secretAccessKey: String = "",
  ) {
    private val arnRegex = Regex("arn:aws:sns:.*:.*:(.*)$")

    val name: String
      get() = if (arn.matches(arnRegex)) arnRegex.find(arn)!!.destructured.component1() else throw InvalidAwsSqsPropertiesException("Topic ARN $arn has an invalid format")
  }

  init {
    queues.forEach { (queueId, queueConfig) ->
      queueIdMustBeLowerCase(queueId)
      queueNamesMustExist(queueId, queueConfig)
      awsQueueSecretsMustExist(queueId, queueConfig)
      localstackTopicSubscriptionsMustExist(queueConfig, queueId)
    }
    topics.forEach { (topicId, topicConfig) ->
      topicIdMustBeLowerCase(topicId)
      awsTopicSecretsMustExist(topicId, topicConfig)
      localstackTopicNameMustExist(topicId, topicConfig)
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

  private fun awsQueueSecretsMustExist(queueId: String, queueConfig: QueueConfig) {
    if (provider == "aws") {
      if (queueConfig.queueAccessKeyId.isEmpty()) throw InvalidAwsSqsPropertiesException("queueId $queueId does not have a queue access key id")
      if (queueConfig.queueSecretAccessKey.isEmpty()) throw InvalidAwsSqsPropertiesException("queueId $queueId does not have a queue secret access key")
      if (queueConfig.dlqName.isNotEmpty()) {
        if (queueConfig.dlqAccessKeyId.isEmpty()) throw InvalidAwsSqsPropertiesException("queueId $queueId does not have a DLQ access key id")
        if (queueConfig.dlqSecretAccessKey.isEmpty()) throw InvalidAwsSqsPropertiesException("queueId $queueId does not have a DLQ secret access key")
      }
    }
  }

  private fun localstackTopicSubscriptionsMustExist(
    queueConfig: QueueConfig,
    queueId: String
  ) {
    if (provider == "localstack") {
      if (queueConfig.subscribeTopicId.isNotEmpty().and(topics.containsKey(queueConfig.subscribeTopicId).not()))
        throw InvalidAwsSqsPropertiesException("queueId $queueId wants to subscribe to ${queueConfig.subscribeTopicId} but it does not exist")
    }
  }

  private fun topicIdMustBeLowerCase(topicId: String) {
    if (topicId != topicId.lowercase()) throw InvalidAwsSqsPropertiesException("topicId $topicId is not lowercase")
  }

  private fun localstackTopicNameMustExist(topicId: String, topicConfig: TopicConfig) {
    if (provider == "localstack") {
      if (topicConfig.name.isEmpty()) throw InvalidAwsSqsPropertiesException("topicId $topicId does not have a name")
    }
  }

  private fun awsTopicSecretsMustExist(topicId: String, topicConfig: TopicConfig) {
    if (provider == "aws") {
      if (topicConfig.arn.isEmpty()) throw InvalidAwsSqsPropertiesException("topicId $topicId does not have an arn")
      if (topicConfig.accessKeyId.isEmpty()) throw InvalidAwsSqsPropertiesException("topicId $topicId does not have an access key id")
      if (topicConfig.secretAccessKey.isEmpty()) throw InvalidAwsSqsPropertiesException("topicId $topicId does not have a secret access key")
    }
  }

  private fun checkForAwsDuplicateValues() {
    if (provider == "aws") {
      mustNotContainDuplicates("queue names", queues, secret = false) { it.value.queueName }
      mustNotContainDuplicates("queue access key ids", queues) { it.value.queueAccessKeyId }
      mustNotContainDuplicates("queue secret access keys", queues) { it.value.queueSecretAccessKey }

      mustNotContainDuplicates("dlq names", queues, secret = false) { it.value.dlqName }
      mustNotContainDuplicates("dlq access key ids", queues) { it.value.dlqAccessKeyId }
      mustNotContainDuplicates("dlq secret access keys", queues) { it.value.dlqSecretAccessKey }

      mustNotContainDuplicates("topic arns", topics, secret = false) { it.value.arn }
      mustNotContainDuplicates("topic access key ids", topics) { it.value.accessKeyId }
      mustNotContainDuplicates("topic secret access keys", topics) { it.value.secretAccessKey }
    }
  }

  private fun checkForLocalStackDuplicateValues() {
    if (provider == "localstack") {
      mustNotContainDuplicates("queue names", queues, secret = false) { it.value.queueName }
      mustNotContainDuplicates("dlq names", queues, secret = false) { it.value.dlqName }
      mustNotContainDuplicates("topic names", topics, secret = false) { it.value.name }
    }
  }

  private fun <T> mustNotContainDuplicates(description: String, source: Map<String, T>, secret: Boolean = true, valueFinder: (Map.Entry<String, T>) -> String) {
    val duplicateValues = source.mapValues(valueFinder).values.filter { it.isNotEmpty() }.groupingBy { it }.eachCount().filterValues { it > 1 }
    if (duplicateValues.isNotEmpty()) {
      val outputValues = if (secret.not()) duplicateValues.keys else duplicateValues.keys.map { "${it.subSequence(0, 4)}******" }.toList()
      throw InvalidAwsSqsPropertiesException("Found duplicated $description: $outputValues")
    }
  }
}

class InvalidAwsSqsPropertiesException(message: String) : IllegalStateException(message)
