package uk.gov.gdx.datashare.queue

import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest

class AwsQueue(
  val id: String,
  val sqsClient: SqsClient,
  val queueName: String,
  val sqsDlqClient: SqsClient? = null,
  val dlqName: String? = null,
) {
  val queueUrl: String by lazy { sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build()).queueUrl() }
  val dlqUrl by lazy { sqsDlqClient?.getQueueUrl(GetQueueUrlRequest.builder().queueName(dlqName).build())?.queueUrl() }
}
