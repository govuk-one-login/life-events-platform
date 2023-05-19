package uk.gov.gdx.datashare.services

import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.DeleteQueueRequest
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest

@Service
class QueueService {
  private val sqsClient by lazy { SqsClient.builder().build() }

  fun deleteQueue(queueName: String) {
    val queueUrl = getQueueUrl(queueName)
    val deleteQueueRequest = DeleteQueueRequest.builder().queueUrl(queueUrl).build()
    sqsClient.deleteQueue(deleteQueueRequest)
  }

  private fun getQueueUrl(queueName: String): String {
    val getQueueUrlRequest = GetQueueUrlRequest
      .builder()
      .queueName(queueName)
      .build()
    return sqsClient.getQueueUrl(getQueueUrlRequest).queueUrl()
  }
}
