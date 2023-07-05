package uk.gov.gdx.datashare.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.cloudwatch.model.*
import software.amazon.awssdk.services.kms.KmsClient
import software.amazon.awssdk.services.kms.model.*
import software.amazon.awssdk.services.kms.model.Tag
import software.amazon.awssdk.services.kms.model.TagResourceRequest
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.*
import software.amazon.awssdk.services.sts.StsClient
import uk.gov.gdx.datashare.queue.AwsQueue
import uk.gov.gdx.datashare.queue.AwsQueueFactory
import uk.gov.gdx.datashare.queue.SqsProperties
import uk.gov.gdx.datashare.repositories.AcquirerSubscriptionRepository
import java.time.Instant

@Service
class OutboundEventQueueService(
  private val acquirerSubscriptionRepository: AcquirerSubscriptionRepository,
  private val awsQueueFactory: AwsQueueFactory,
  private val sqsProperties: SqsProperties,
  @Value("\${environment}") private val environment: String,
  @Value("\${task-role-arn}") private val taskRoleArn: String,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  private val awsQueues: MutableMap<String, AwsQueue> = mutableMapOf()
  private val accountId by lazy { StsClient.create().callerIdentity.account() }
  private val cloudWatchClient by lazy { CloudWatchClient.create() }
  private val kmsClient by lazy { KmsClient.create() }
  private val sqsClient by lazy { SqsClient.create() }

  fun sendMessage(queueName: String, message: String, id: String) {
    val queue = getQueue(queueName)
    val request = SendMessageRequest.builder()
      .queueUrl(queue.queueUrl)
      .messageBody(message)

    if (isFifoQueue(queueName)) {
      request.messageGroupId("default")
        .messageDeduplicationId(id)
    }

    queue.sqsClient.sendMessage(request.build())
  }

  fun createAcquirerQueue(queueName: String, acquirerPrincipal: String): String {
    val dlqName = dlqName(queueName)
    val dlqKeyId = createKeyForQueue(dlqName)
    val dlqUrl = createQueue(dlqName, dlqKeyId, 8)
    val dlqArn = getQueueArn(dlqUrl)
    val keyId = createKeyForQueue(queueName, acquirerPrincipal)
    return createQueue(queueName, keyId, 4, dlqArn, acquirerPrincipal)
  }

  fun deleteAcquirerQueueAndDlq(queueName: String) {
    val dlqName = dlqName(queueName)
    log.info("Deleting queues: $queueName and $dlqName")
    deleteQueueAndKey(queueName)
    deleteQueueAndKey(dlqName)
  }

  fun getMetrics(): Map<String, QueueMetric> {
    val allQueueNames = acquirerSubscriptionRepository.findAllByQueueNameIsNotNullAndWhenDeletedIsNull().mapNotNull {
      it.queueName
    }
    if (allQueueNames.isEmpty()) {
      return emptyMap()
    }

    return getQueueMetrics(allQueueNames)
  }

  private fun createKeyForQueue(queueName: String, acquirerPrincipal: String? = null): String {
    val keyRequest = CreateKeyRequest.builder()
      .description("Key for sqs queue $queueName in environment $environment")
      .policy(keyPolicy(acquirerPrincipal))
      .build()
    val createKeyResponse = kmsClient.createKey(keyRequest)
    val keyId = createKeyResponse.keyMetadata().keyId()
    enableKeyRotation(keyId)
    aliasKey(queueName, keyId)
    tagKey(keyId)

    return keyId
  }

  private fun enableKeyRotation(keyId: String) {
    val rotationRequest = EnableKeyRotationRequest.builder()
      .keyId(keyId)
      .build()
    kmsClient.enableKeyRotation(rotationRequest)
  }

  private fun aliasKey(queueName: String, keyId: String?) {
    val aliasRequest = CreateAliasRequest.builder()
      .aliasName(generateAlias(queueName))
      .targetKeyId(keyId)
      .build()
    kmsClient.createAlias(aliasRequest)
  }

  private fun generateAlias(queueName: String) = "alias/$environment/sqs-$queueName"

  private fun tagKey(keyId: String?) {
    val tagRequest = TagResourceRequest.builder()
      .keyId(keyId)
      .tags(
        Tag.builder().tagKey("Environment").tagValue(environment).build(),
        Tag.builder().tagKey("Owner").tagValue("di-life-events-platform@digital.cabinet-office.gov.uk").build(),
        Tag.builder().tagKey("Product").tagValue("DI Life Events Platform").build(),
        Tag.builder().tagKey("Repository").tagValue("https://github.com/alphagov/di-data-life-events-platform").build(),
        Tag.builder().tagKey("Source").tagValue("Application").build(),
      ).build()
    kmsClient.tagResource(tagRequest)
  }

  private fun createQueue(
    queueName: String,
    keyId: String,
    messageRetentionDays: Int,
    dlqArn: String? = null,
    acquirerPrincipal: String? = null,
  ): String {
    val createQueueRequest = CreateQueueRequest.builder()
      .queueName(queueName)
      .attributes(buildQueueAttributes(keyId, queueName, messageRetentionDays, dlqArn, acquirerPrincipal))
      .build()
    val createQueueResponse = sqsClient.createQueue(createQueueRequest)
    return createQueueResponse.queueUrl()
  }

  private fun buildQueueAttributes(
    keyId: String,
    queueName: String,
    messageRetentionDays: Int,
    dlqArn: String?,
    acquirerPrincipal: String?,
  ): HashMap<QueueAttributeName, String> {
    val attributes = HashMap<QueueAttributeName, String>()
    attributes[QueueAttributeName.KMS_MASTER_KEY_ID] = keyId
    attributes[QueueAttributeName.KMS_DATA_KEY_REUSE_PERIOD_SECONDS] = "300" // 5 minutes
    attributes[QueueAttributeName.MESSAGE_RETENTION_PERIOD] = "${messageRetentionDays * 24 * 60 * 60}"
    attributes[QueueAttributeName.VISIBILITY_TIMEOUT] = "30"
    attributes[QueueAttributeName.POLICY] = queuePolicy(acquirerPrincipal)
    if (isFifoQueue(queueName)) {
      attributes[QueueAttributeName.FIFO_QUEUE] = "true"
    }
    if (dlqArn != null) {
      attributes[QueueAttributeName.REDRIVE_POLICY] = """{"maxReceiveCount": "10", "deadLetterTargetArn": "$dlqArn"}"""
    }
    return attributes
  }

  private fun getQueueArn(queueUrl: String) = getQueueAttribute(queueUrl, QueueAttributeName.QUEUE_ARN)
  private fun getQueueKmsId(queueUrl: String) = getQueueAttribute(queueUrl, QueueAttributeName.KMS_MASTER_KEY_ID)

  private fun getQueueAttribute(queueUrl: String, queueAttributeName: QueueAttributeName): String? {
    val request = GetQueueAttributesRequest.builder()
      .queueUrl(queueUrl)
      .attributeNames(queueAttributeName)
      .build()
    val response = sqsClient.getQueueAttributes(request)
    return response.attributes()[queueAttributeName]
  }

  private fun isFifoQueue(queueName: String) = queueName.endsWith(".fifo")

  private fun dlqName(queueName: String) =
    if (isFifoQueue(queueName)) "${queueName.substringBefore(".fifo")}_dlq.fifo" else "${queueName}_dlq"

  private fun keyPolicy(acquirerPrincipal: String?): String {
    if (acquirerPrincipal == null) {
      return """
        {
          "Version": "2012-10-17",
          "Id": "key-default-1",
          "Statement": [
            {
              "Sid": "Enable IAM User Permissions",
              "Effect": "Allow",
              "Principal": {
                "AWS": "arn:aws:iam::$accountId:root"
              },
              "Action": "kms:*",
              "Resource": "*"
            },
            {
              "Sid": "Give application control",
              "Effect": "Allow",
              "Principal": {
                "AWS": "$taskRoleArn"
              },
              "Action": "kms:*",
              "Resource": "*"
            }
          ]
        }
      """.trimIndent()
    }
    return """
      {
        "Version": "2012-10-17",
        "Id": "key-default-1",
        "Statement": [
          {
            "Sid": "Enable IAM User Permissions",
            "Effect": "Allow",
            "Principal": {
              "AWS": "arn:aws:iam::$accountId:root"
            },
            "Action": "kms:*",
            "Resource": "*"
          },
          {
            "Sid": "Give application control",
            "Effect": "Allow",
            "Principal": {
              "AWS": "$taskRoleArn"
            },
            "Action": "kms:*",
            "Resource": "*"
          },
          {
            "Sid": "Acquirer access",
            "Effect": "Allow",
            "Principal": {
              "AWS": "$acquirerPrincipal"
            },
            "Action": [
              "kms:GenerateDataKey",
              "kms:Decrypt"
            ],
            "Resource": "*"
          }
        ]
      }
    """.trimIndent()
  }

  private fun queuePolicy(acquirerPrincipal: String?): String {
    if (acquirerPrincipal == null) {
      return """
        {
          "Version": "2012-10-17",
          "Id": "cross-account-access",
          "Statement": [
            {
              "Sid": "httpsonly",
              "Effect": "Deny",
              "Action": "sqs:*",
              "Resource": "*",
              "Condition": {
                "Bool": {
                  "aws:SecureTransport": "false"
                }
              }
            }
          ]
        }
      """.trimIndent()
    }
    return """
      {
        "Version": "2012-10-17",
        "Id": "cross-account-access",
        "Statement": [
          {
            "Sid": "acquirerAccess",
            "Principal": {
              "AWS": "$acquirerPrincipal"
            },
            "Effect": "Allow",
            "Resource": "*",
            "Action": [
              "SQS:GetQueueUrl",
              "SQS:GetQueueAttributes",
              "SQS:ChangeMessageVisibility",
              "SQS:ReceiveMessage",
              "SQS:DeleteMessage"
            ]
          },
          {
            "Sid": "httpsonly",
            "Effect": "Deny",
            "Action": "sqs:*",
            "Resource": "*",
            "Condition": {
              "Bool": {
                "aws:SecureTransport": "false"
              }
            }
          }
        ]
      }
    """.trimIndent()
  }

  private fun deleteQueueAndKey(queueName: String) {
    val queue: AwsQueue;
    try {
      queue = getQueue(queueName)
    } catch (_: QueueDoesNotExistException) {
      log.info("Deleting queue $queueName failed, queue does not exist")
      return
    }
    val kmsId = getQueueKmsId(queue.queueUrl)

    val deleteQueueRequest = DeleteQueueRequest.builder().queueUrl(queue.queueUrl).build()
    queue.sqsClient.deleteQueue(deleteQueueRequest)

    val scheduleKeyDeletionRequest = ScheduleKeyDeletionRequest.builder()
      .keyId(kmsId)
      .pendingWindowInDays(7)
      .build()
    kmsClient.scheduleKeyDeletion(scheduleKeyDeletionRequest)
  }

  private fun getQueue(queueName: String): AwsQueue {
    return awsQueues.computeIfAbsent(queueName) {
      val queueConfig = SqsProperties.QueueConfig(queueName = it)
      val sqsClient = awsQueueFactory.getOrDefaultSqsClient(it, queueConfig, sqsProperties, null)
      AwsQueue(it, sqsClient, it)
    }
  }

  private fun getQueueMetrics(queueNames: List<String>): Map<String, QueueMetric> {
    val dlqLengthQueries = queueNames.map {
      buildMetricDataQuery("ApproximateNumberOfMessagesVisible", dlqName(it))
    }
    val ageOfOldestMessageQueries = queueNames.map {
      buildMetricDataQuery("ApproximateAgeOfOldestMessage", it)
    }

    val getMetricDataRequest = GetMetricDataRequest.builder()
      .metricDataQueries(dlqLengthQueries + ageOfOldestMessageQueries)
      .startTime(Instant.now().minusSeconds(120))
      .endTime(Instant.now())
      .build()

    val metricDataResults = cloudWatchClient.getMetricData(getMetricDataRequest).metricDataResults()
    return queueNames.associateWith { queueName ->
      QueueMetric(
        metricDataResults.find { it.id() == sanitizeMetricId(queueName) }?.values()?.firstOrNull()?.toInt(),
        metricDataResults.find { it.id() == sanitizeMetricId(dlqName(queueName)) }?.values()?.firstOrNull()?.toInt(),
      )
    }
  }

  private fun buildMetricDataQuery(metricName: String, queueName: String) =
    MetricDataQuery.builder()
      .metricStat(
        MetricStat.builder()
          .metric(
            Metric.builder()
              .namespace("AWS/SQS")
              .metricName(metricName)
              .dimensions(Dimension.builder().name("QueueName").value(queueName).build())
              .build(),
          )
          .period(60)
          .stat("Maximum")
          .build(),
      )
      .id(sanitizeMetricId(queueName))
      .build()

  private fun sanitizeMetricId(id: String): String = id.replace("-", "_")
}

data class QueueMetric(
  val ageOfOldestMessage: Int?,
  val dlqLength: Int?,
)
