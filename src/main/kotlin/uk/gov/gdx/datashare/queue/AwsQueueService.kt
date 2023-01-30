package uk.gov.gdx.datashare.queue

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.*
import kotlin.math.min
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest as AwsPurgeQueueRequest

open class AwsQueueService(
  awsQueueFactory: AwsQueueFactory,
  sqsProperties: SqsProperties,
  private val objectMapper: ObjectMapper,
) {

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  private val awsQueues: List<AwsQueue> = awsQueueFactory.createAwsQueues(sqsProperties)

  open fun findByQueueId(queueId: String) = awsQueues.associateBy { it.id }.getOrDefault(queueId, null)
  open fun findByQueueName(queueName: String) = awsQueues.associateBy { it.queueName }.getOrDefault(queueName, null)
  open fun findByDlqName(dlqName: String) = awsQueues.associateBy { it.dlqName }.getOrDefault(dlqName, null)

  open fun retryDlqMessages(request: RetryDlqRequest): RetryDlqResult =
    request.awsQueue.retryDlqMessages()

  open fun getDlqMessages(request: GetDlqRequest): GetDlqResult =
    request.awsQueue.getDlqMessages(request.maxMessages)

  open fun retryAllDlqs() =
    awsQueues
      .map { awsQueue -> RetryDlqRequest(awsQueue) }
      .map { retryDlqRequest -> retryDlqMessages(retryDlqRequest) }

  private fun AwsQueue.retryDlqMessages(): RetryDlqResult {
    if (sqsDlqClient == null || dlqUrl == null) return RetryDlqResult(0, mutableListOf())
    val messageCount = sqsDlqClient.countMessagesOnQueue(dlqUrl!!)
    val messages = mutableListOf<Message>()
    repeat(messageCount) {
      sqsDlqClient.receiveMessage(
        ReceiveMessageRequest.builder().queueUrl(dlqUrl).maxNumberOfMessages(1).messageAttributeNames("All").build(),
      ).messages().firstOrNull()
        ?.also { msg ->
          sqsClient.sendMessage(
            SendMessageRequest.builder().queueUrl(queueUrl).messageBody(msg.body())
              .messageAttributes(msg.messageAttributes()).build(),
          )
          sqsDlqClient.deleteMessage(DeleteMessageRequest.builder().queueUrl(dlqUrl).receiptHandle(msg.receiptHandle()).build())
          messages += msg
        }
    }
    messageCount.takeIf { it > 0 }
      ?.also { log.info("For dlq ${this.dlqName} we found $messageCount messages, attempted to retry ${messages.size}") }
    return RetryDlqResult(messageCount, messages.toList())
  }

  private fun AwsQueue.getDlqMessages(maxMessages: Int): GetDlqResult {
    if (sqsDlqClient == null || dlqUrl == null) return GetDlqResult(0, 0, mutableListOf())

    val messages = mutableListOf<DlqMessage>()
    val messageCount = sqsDlqClient.countMessagesOnQueue(dlqUrl!!)
    val messagesToReturnCount = min(messageCount, maxMessages)

    repeat(messagesToReturnCount) {
      sqsDlqClient.receiveMessage(ReceiveMessageRequest.builder().queueUrl(dlqUrl).maxNumberOfMessages(1).build()).messages().firstOrNull()
        ?.also { msg ->
          val map: Map<String, Any> = HashMap()
          messages += DlqMessage(messageId = msg.messageId(), body = objectMapper.readValue(msg.body(), map.javaClass))
        }
    }

    return GetDlqResult(messageCount, messagesToReturnCount, messages.toList())
  }

  open fun purgeQueue(request: PurgeQueueRequest): PurgeQueueResult =
    with(request) {
      sqsClient.countMessagesOnQueue(queueUrl)
        .takeIf { it > 0 }
        ?.also { sqsClient.purgeQueue(AwsPurgeQueueRequest.builder().queueUrl(queueUrl).build()) }
        ?.also { log.info("For queue $queueName attempted to purge $it messages from queue") }
        ?.let { PurgeQueueResult(it) }
        ?: PurgeQueueResult(0)
    }

  open fun findQueueToPurge(queueName: String): PurgeQueueRequest? =
    findByQueueName(queueName)
      ?.let { awsQueue -> PurgeQueueRequest(awsQueue.queueName, awsQueue.sqsClient, awsQueue.queueUrl) }
      ?: findByDlqName(queueName)
        ?.let { awsQueue -> PurgeQueueRequest(awsQueue.dlqName!!, awsQueue.sqsDlqClient!!, awsQueue.dlqUrl!!) }
}

data class RetryDlqRequest(val awsQueue: AwsQueue)
data class RetryDlqResult(val messagesFoundCount: Int, val messages: List<Message>)

data class GetDlqRequest(val awsQueue: AwsQueue, val maxMessages: Int)
data class GetDlqResult(val messagesFoundCount: Int, val messagesReturnedCount: Int, val messages: List<DlqMessage>)
data class DlqMessage(val body: Map<String, Any>, val messageId: String)

data class PurgeQueueRequest(val queueName: String, val sqsClient: SqsClient, val queueUrl: String)
data class PurgeQueueResult(val messagesFoundCount: Int)

internal fun SqsClient.countMessagesOnQueue(queueUrl: String): Int =
  this.getQueueAttributes(GetQueueAttributesRequest.builder().queueUrl(queueUrl).attributeNames(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES).build())
    .let { it.attributes()[QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES]?.toInt() ?: 0 }
