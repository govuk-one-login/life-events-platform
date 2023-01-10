package uk.gov.gdx.datashare.queue

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AmazonSnsFactory {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun awsSnsClient(topicId: String, region: String): AmazonSNS =
    awsAmazonSNS(region)
      .also { log.info("Created an AWS SNS client for topicId=$topicId") } as AmazonSNS

  fun localstackSnsClient(topicId: String, localstackUrl: String, region: String): AmazonSNS =
    localstackAmazonSNS(localstackUrl, region)
      .also { log.info("Created a LocalStack SNS client for topicId=$topicId") }

  private fun awsAmazonSNS(region: String) =
    AmazonSNSClientBuilder.standard()
      .withCredentials(DefaultAWSCredentialsProviderChain())
      .withRegion(region)
      .build()

  private fun localstackAmazonSNS(localstackUrl: String, region: String) =
    AmazonSNSClientBuilder.standard()
      .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(localstackUrl, region))
      .withCredentials(AWSStaticCredentialsProvider(BasicAWSCredentials("any", "any"))) // LocalStack doesn't work with Anonymous credentials when dealing with topics but doesn't care what the credential values are.
      .build()
}
