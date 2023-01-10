package uk.gov.gdx.datashare.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated

@ConstructorBinding
@ConfigurationProperties(prefix = "api.base.s3")
@Validated
data class S3Config(
  val localstackUrl: String = "",
  val region: String = "",
  val ingressBucket: String,
  val ingressArchiveBucket: String,
  val egressBucket: String,
)
