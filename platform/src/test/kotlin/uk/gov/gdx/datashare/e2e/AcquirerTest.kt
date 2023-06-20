package uk.gov.gdx.datashare.e2e

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.e2e.http.Api
import java.util.*

@Tag("E2E")
class AcquirerTest {
  private val underTest = Api()

  @Test
  fun `create and delete acquirer with cognito client`() {
    val clientName = UUID.randomUUID().toString()
    val createResponse = underTest.createAcquirerWithCognitoClient(clientName)

    assertThat(createResponse.clientName).isEqualTo(clientName)
    assertThat(createResponse.clientId).isNotNull()
    assertThat(createResponse.clientSecret).isNotNull()
    assertThat(createResponse.queueUrl).isNull()

    val acquirers = underTest.getAcquirers()
    assertThat(acquirers.filter { it.name == clientName }).hasSize(1)

    val acquirer = acquirers.find { it.name == clientName }!!

    val newAcquirerSubscriptions = underTest.getAcquirerSubscriptionsForAcquirer(acquirer.id)
    assertThat(newAcquirerSubscriptions).hasSize(1)
    assertThat(newAcquirerSubscriptions).first().hasFieldOrPropertyWithValue("oauthClientId", createResponse.clientId)
    assertThat(newAcquirerSubscriptions).first().hasFieldOrPropertyWithValue("queueName", null)
    assertThat(newAcquirerSubscriptions).first().hasFieldOrPropertyWithValue("queueUrl", null)

    underTest.deleteAcquirer(acquirer.id)

    val remainingAcquirerSubscriptions = underTest.getAcquirerSubscriptions()
    val remainingAcquirers = underTest.getAcquirers()
    assertThat(remainingAcquirers.filter { it.name == clientName }).isEmpty()
    assertThat(remainingAcquirerSubscriptions.filter { it.acquirerId == acquirer.id }).isEmpty()
  }

  @Test
  fun `create and delete acquirer with queue`() {
    val name = UUID.randomUUID().toString()
    val queueName = "acq_${Config.environment}_test-$name"
    val principalArn = "arn:aws:iam::776473272850:role/${Config.environment}-task-role"
    val createResponse = underTest.createAcquirerWithQueue(name, queueName, principalArn)

    assertThat(createResponse.clientName).isNull()
    assertThat(createResponse.clientId).isNull()
    assertThat(createResponse.clientSecret).isNull()
    assertThat(createResponse.queueUrl).isNotNull()

    val acquirers = underTest.getAcquirers()
    assertThat(acquirers.filter { it.name == name }).hasSize(1)

    val acquirer = acquirers.find { it.name == name }!!

    val newAcquirerSubscriptions = underTest.getAcquirerSubscriptionsForAcquirer(acquirer.id)
    assertThat(newAcquirerSubscriptions).hasSize(1)
    assertThat(newAcquirerSubscriptions).first().hasFieldOrPropertyWithValue("oauthClientId", null)
    assertThat(newAcquirerSubscriptions).first().hasFieldOrPropertyWithValue("queueName", queueName)

    underTest.deleteAcquirer(acquirer.id)

    val remainingAcquirerSubscriptions = underTest.getAcquirerSubscriptions()
    val remainingAcquirers = underTest.getAcquirers()
    assertThat(remainingAcquirers.filter { it.name == name }).isEmpty()
    assertThat(remainingAcquirerSubscriptions.filter { it.acquirerId == acquirer.id }).isEmpty()
  }
}
