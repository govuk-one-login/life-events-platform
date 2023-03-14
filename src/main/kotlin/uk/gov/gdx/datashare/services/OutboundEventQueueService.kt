package uk.gov.gdx.datashare.services

import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.gdx.datashare.queue.AwsQueue
import uk.gov.gdx.datashare.queue.AwsQueueFactory
import uk.gov.gdx.datashare.queue.SqsProperties

@Service
class OutboundEventQueueService(
  private val awsQueueFactory: AwsQueueFactory,
  private val sqsProperties: SqsProperties,
) {
  private val awsQueues: MutableMap<String, AwsQueue> = mutableMapOf()

  fun sendMessage(queueName: String, message: String) {
    val queue = getQueue(queueName)
    val request = SendMessageRequest.builder()
      .queueUrl(queue.queueUrl)
      .messageBody(message)
      .build()
    queue.sqsClient.sendMessage(request)
  }

  private fun getQueue(queueName: String): AwsQueue {
    return awsQueues.computeIfAbsent(queueName) {
      val queueConfig = SqsProperties.QueueConfig(queueName = it)
      val sqsClient = awsQueueFactory.getOrDefaultSqsClient(it, queueConfig, sqsProperties, null)
      AwsQueue(it, sqsClient, it)
    }
  }
}
