package uk.gov.gdx.datashare.queue

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.jms.annotation.EnableJms

@Configuration
@EnableConfigurationProperties(SqsProperties::class)
@EnableJms
@AutoConfigureBefore(HealthEndpointAutoConfiguration::class)
class SqsConfiguration {

  @Bean
  @ConditionalOnMissingBean
  fun awsTopicFactory(applicationContext: ConfigurableApplicationContext) =
    AwsTopicFactory(applicationContext, AmazonSnsFactory())

  @Bean
  @ConditionalOnMissingBean
  fun awsQueueFactory(applicationContext: ConfigurableApplicationContext) =
    AwsQueueFactory(applicationContext, AmazonSqsFactory())

  @Bean
  @ConditionalOnMissingBean
  fun awsQueueService(
    awsTopicFactory: AwsTopicFactory,
    awsQueueFactory: AwsQueueFactory,
    sqsProperties: SqsProperties,
    objectMapper: ObjectMapper,
  ) = AwsQueueService(awsTopicFactory, awsQueueFactory, sqsProperties, objectMapper)

  @Bean
  @ConditionalOnMissingBean
  fun awsQueueControllerAsync(awsQueueService: AwsQueueService) = AwsQueueControllerAsync(awsQueueService)

  @Bean
  @ConditionalOnMissingBean
  @DependsOn("awsQueueService")
  fun awsQueueContainerFactoryProxy(factories: List<AwsQueueDestinationContainerFactory>) =
    AwsQueueJmsListenerContainerFactory(factories)
}
