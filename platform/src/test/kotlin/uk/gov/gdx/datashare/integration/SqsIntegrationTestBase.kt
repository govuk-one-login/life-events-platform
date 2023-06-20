package uk.gov.gdx.datashare.integration

import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import uk.gov.gdx.datashare.integration.LocalStackContainer.setLocalStackProperties
import uk.gov.gdx.datashare.queue.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SqsIntegrationTestBase : IntegrationTestBase() {
  @Autowired
  private lateinit var awsQueueService: AwsQueueService

  @Autowired
  lateinit var webTestClient: WebTestClient

  protected val supplierEventQueue by lazy { awsQueueService.findByQueueId("supplierevent") as AwsQueue }
  protected val acquirerEventQueue by lazy { awsQueueService.findByQueueId("acquirerevent") as AwsQueue }

  @BeforeEach
  fun cleanQueue() {
    supplierEventQueue.sqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(supplierEventQueue.queueUrl).build())
    acquirerEventQueue.sqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(acquirerEventQueue.queueUrl).build())
  }

  companion object {
    private val localStackContainer = LocalStackContainer.instance

    @Suppress("unused")
    @JvmStatic
    @DynamicPropertySource
    fun testcontainers(registry: DynamicPropertyRegistry) {
      localStackContainer?.also { setLocalStackProperties(it, registry) }
    }
  }
}
