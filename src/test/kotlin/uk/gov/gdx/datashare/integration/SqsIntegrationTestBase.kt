package uk.gov.gdx.datashare.integration

import com.amazonaws.services.sqs.model.PurgeQueueRequest
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import uk.gov.gdx.datashare.integration.LocalStackContainer.setLocalStackProperties
import uk.gov.gdx.datashare.queue.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SqsIntegrationTestBase : IntegrationTestBase() {

  @Autowired
  private lateinit var awsQueueService: AwsQueueService

  @SpyBean
  protected lateinit var sqsPropertiesSpy: SqsProperties

  protected val auditQueue by lazy { awsQueueService.findByQueueId("audit") as AwsQueue }
  protected val dataProcessorQueue by lazy { awsQueueService.findByQueueId("dataprocessor") as AwsQueue }

  @BeforeEach
  fun cleanQueue() {
    auditQueue.sqsClient.purgeQueue(PurgeQueueRequest(auditQueue.queueUrl))
    dataProcessorQueue.sqsClient.purgeQueue(PurgeQueueRequest(dataProcessorQueue.queueUrl))
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
