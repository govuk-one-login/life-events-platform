package uk.gov.gdx.datashare.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.kms.KmsClient
import software.amazon.awssdk.services.kms.model.CreateAliasRequest
import software.amazon.awssdk.services.kms.model.CreateKeyRequest
import software.amazon.awssdk.services.kms.model.Tag
import software.amazon.awssdk.services.kms.model.TagResourceRequest
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest
import software.amazon.awssdk.services.sqs.model.DeleteQueueRequest
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest
import software.amazon.awssdk.services.sqs.model.QueueAttributeName
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sts.StsClient
import uk.gov.gdx.datashare.queue.AwsQueue
import uk.gov.gdx.datashare.queue.AwsQueueFactory
import uk.gov.gdx.datashare.queue.SqsProperties

@Service
class OutboundEventQueueService(
  private val awsQueueFactory: AwsQueueFactory,
  private val sqsProperties: SqsProperties,
  @Value("\${environment}") private val environment: String,
  @Value("\${task-role-arn}") private val taskRoleArn: String,
) {
  private val awsQueues: MutableMap<String, AwsQueue> = mutableMapOf()
  private val accountId by lazy { StsClient.create().callerIdentity.account() }
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

  fun deleteQueue(queueName: String) {
    val queue = getQueue(queueName)
    val deleteQueueRequest = DeleteQueueRequest.builder().queueUrl(queue.queueUrl).build()
    sqsClient.deleteQueue(deleteQueueRequest)
  }

  private fun createKeyForQueue(queueName: String, acquirerPrincipal: String? = null): String {
    val keyRequest = CreateKeyRequest.builder()
      .description("Key for sqs queue $queueName in environment $environment")
      .policy(keyPolicy(acquirerPrincipal))
      .build()
    val createKeyResponse = kmsClient.createKey(keyRequest)
    val keyId = createKeyResponse.keyMetadata().keyId()

    aliasKey(queueName, keyId)
    tagKey(keyId)

    return keyId
  }

  private fun aliasKey(queueName: String, keyId: String?) {
    val aliasRequest = CreateAliasRequest.builder()
      .aliasName("$environment/sqs-$queueName")
      .targetKeyId(keyId)
      .build()
    kmsClient.createAlias(aliasRequest)
  }

  private fun tagKey(keyId: String?) {
    val tagRequest = TagResourceRequest.builder()
      .keyId(keyId)
      .tags(
        Tag.builder().tagKey("Environment").tagValue(environment).build(),
        Tag.builder().tagKey("Owner").tagValue("gdx-dev-team@digital.cabinet-office.gov.uk").build(),
        Tag.builder().tagKey("Product").tagValue("Government Data Exchange").build(),
        Tag.builder().tagKey("Repository").tagValue("https://github.com/alphagov/gdx-data-share-poc").build(),
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

  private fun getQueueArn(queueUrl: String): String? {
    val request = GetQueueAttributesRequest.builder()
      .queueUrl(queueUrl)
      .attributeNames(QueueAttributeName.QUEUE_ARN)
      .build()
    val response = sqsClient.getQueueAttributes(request)
    return response.attributes()[QueueAttributeName.QUEUE_ARN]
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
          "Version": "2012-10-12",
          "Id": "cross-account-access",
          "Statement": [
            {
              "Sid": "httpsonly",
              "Effect": "Deny",
              "Action": "sqs:*",
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
        "Version": "2012-10-12",
        "Id": "cross-account-access",
        "Statement": [
          {
            "Sid": "acquirerAccess",
            "Principal": {
              "AWS": "$acquirerPrincipal"
            },
            "Effect": "Allow",
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

  private fun getQueue(queueName: String): AwsQueue {
    return awsQueues.computeIfAbsent(queueName) {
      val queueConfig = SqsProperties.QueueConfig(queueName = it)
      val sqsClient = awsQueueFactory.getOrDefaultSqsClient(it, queueConfig, sqsProperties, null)
      AwsQueue(it, sqsClient, it)
    }
  }
}
