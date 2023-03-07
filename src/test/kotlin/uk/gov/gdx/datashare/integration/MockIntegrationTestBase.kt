package uk.gov.gdx.datashare.uk.gov.gdx.datashare.integration

import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import uk.gov.gdx.datashare.integration.IntegrationTestBase
import uk.gov.gdx.datashare.queue.AwsQueueService
import uk.gov.gdx.datashare.services.AcquirerEventProcessor
import uk.gov.gdx.datashare.services.EventAcceptorService
import uk.gov.gdx.datashare.services.PrisonerEventMessageProcessor
import uk.gov.gdx.datashare.services.SupplierEventProcessor
import java.util.*

/**
 * Base class for MockMvc tests
 *
 * Creates a mock servlet environment instead of a "real" server listening on a real port. This means that we can share
 * db transactions between test and application code.
 *
 * This also mocks out all queue related code to make these tests more lightweight.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@ExtendWith(WireMockExtension::class)
@AutoConfigureMockMvc
abstract class MockIntegrationTestBase : IntegrationTestBase() {
  @MockBean
  @Suppress("unused")
  private lateinit var eventAcceptorService: EventAcceptorService

  @MockBean
  @Suppress("unused")
  private lateinit var supplierEventProcessor: SupplierEventProcessor

  @MockBean
  @Suppress("unused")
  private lateinit var acquirerEventProcessor: AcquirerEventProcessor

  @MockBean
  @Suppress("unused")
  private lateinit var prisonerEventMessageProcessor: PrisonerEventMessageProcessor

  @MockBean
  @Suppress("unused")
  private lateinit var awsQueueService: AwsQueueService
}
