package uk.gov.gdx.datashare.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.CreateBucketRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class S3ClientsConfig(private val s3Config: S3Config) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
  @Bean
  @ConditionalOnExpression("T(org.springframework.util.StringUtils).isEmpty('\${api.base.s3.localstack-url:}')")
  fun amazonS3(): AmazonS3 =
    AmazonS3ClientBuilder.standard()
      .withRegion(s3Config.region)
      .build()

  @Bean
  @ConditionalOnProperty(name = ["api.base.s3.localstack-url"])
  fun amazonS3Test(): AmazonS3 =
    AmazonS3ClientBuilder.standard()
      .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(s3Config.localstackUrl, s3Config.region))
      .withCredentials(AWSStaticCredentialsProvider(BasicAWSCredentials("any", "any")))
      .build()
      .also {
        val request = CreateBucketRequest(s3Config.ingressBucket, s3Config.region)
        runCatching {
          it.createBucket(request)
        }.onFailure { log.info("Failed to create S3 bucket ${s3Config.ingressBucket} due to error ${it.message}") }
      }
      .also {
        val request = CreateBucketRequest(s3Config.ingressArchiveBucket, s3Config.region)
        runCatching {
          it.createBucket(request)
        }.onFailure { log.info("Failed to create S3 bucket ${s3Config.ingressArchiveBucket} due to error ${it.message}") }
      }
      .also {
        val request = CreateBucketRequest(s3Config.egressBucket, s3Config.region)
        runCatching {
          it.createBucket(request)
        }.onFailure { log.info("Failed to create S3 bucket ${s3Config.egressBucket} due to error ${it.message}") }
      }
}
