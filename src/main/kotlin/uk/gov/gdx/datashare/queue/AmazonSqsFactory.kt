package uk.gov.gdx.datashare.queue

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.AnonymousAWSCredentials
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AmazonSqsFactory {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun awsSqsClient(queueId: String, queueName: String, region: String): AmazonSQS =
    awsAmazonSQS(region)
      .also { log.info("Created an AWS SQS client for queueId $queueId with name $queueName") }

  fun localStackSqsClient(queueId: String, queueName: String, localstackUrl: String, region: String): AmazonSQS =
    localStackAmazonSQS(localstackUrl, region)
      .also { log.info("Created a LocalStack SQS client for queueId $queueId with name $queueName") }

  fun awsSqsDlqClient(queueId: String, dlqName: String, region: String): AmazonSQS =
    awsAmazonSQS(region)
      .also { log.info("Created an AWS SQS DLQ client for queueId $queueId with name $dlqName") }

  fun localStackSqsDlqClient(queueId: String, dlqName: String, localstackUrl: String, region: String): AmazonSQS =
    localStackAmazonSQS(localstackUrl, region)
      .also { log.info("Created a LocalStack SQS DLQ client for queueId $queueId with name $dlqName") }

  private fun awsAmazonSQS(region: String) =
    AmazonSQSClientBuilder.standard()
      .withCredentials(DefaultAWSCredentialsProviderChain())
      .withRegion(region)
      .build()

  private fun localStackAmazonSQS(localstackUrl: String, region: String) =
    AmazonSQSClientBuilder.standard()
      .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(localstackUrl, region))
      .withCredentials(AWSStaticCredentialsProvider(AnonymousAWSCredentials()))
      .build()
}
