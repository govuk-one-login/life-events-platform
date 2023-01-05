package uk.gov.gdx.datashare.queue

import com.amazonaws.services.sqs.AmazonSQS

class HmppsQueue(
  val id: String,
  val sqsClient: AmazonSQS,
  val queueName: String,
  val sqsDlqClient: AmazonSQS? = null,
  val dlqName: String? = null
) {
  val queueUrl: String by lazy { sqsClient.getQueueUrl(queueName).queueUrl }
  val dlqUrl by lazy { sqsDlqClient?.getQueueUrl(dlqName)?.queueUrl }
}
