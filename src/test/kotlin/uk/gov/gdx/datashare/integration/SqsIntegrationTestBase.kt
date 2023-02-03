package uk.gov.gdx.datashare.integration

import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import uk.gov.gdx.datashare.integration.LocalStackContainer.setLocalStackProperties
import uk.gov.gdx.datashare.queue.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SqsIntegrationTestBase : IntegrationTestBase() {

  @Autowired
  private lateinit var awsQueueService: AwsQueueService

  protected val dataProcessorQueue by lazy { awsQueueService.findByQueueId("dataprocessor") as AwsQueue }

  @BeforeEach
  fun cleanQueue() {
    dataProcessorQueue.sqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(dataProcessorQueue.queueUrl).build())
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
