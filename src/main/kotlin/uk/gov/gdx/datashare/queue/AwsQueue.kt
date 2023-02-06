package uk.gov.gdx.datashare.queue

import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest

class AwsQueue(
  val id: String,
  val sqsClient: SqsClient,
  val queueName: String,
  val sqsDlqClient: SqsClient? = null,
  val dlqName: String? = null,
  private val awsAccountId: String? = null,
) {
  val queueUrl: String by lazy { sqsClient.getQueueUrl(buildQueueUrlRequest(queueName, awsAccountId)).queueUrl() }
  val dlqUrl by lazy { sqsDlqClient?.getQueueUrl(buildQueueUrlRequest(dlqName, awsAccountId))?.queueUrl() }

  private fun buildQueueUrlRequest(name: String?, accountId: String?): GetQueueUrlRequest {
    val getQueueUrlRequestBuilder = GetQueueUrlRequest.builder().queueName(name)
    if (accountId != null) {
      getQueueUrlRequestBuilder.queueOwnerAWSAccountId(accountId)
    }
    return getQueueUrlRequestBuilder.build()
  }
}
