package uk.gov.gdx.datashare.queue

import com.amazon.sqs.javamessaging.SQSSession
import jakarta.jms.Destination
import jakarta.jms.Queue
import jakarta.jms.Session
import org.springframework.jms.support.destination.DynamicDestinationResolver

class AwsQueueDestinationResolver(private val sqsProperties: SqsProperties) : DynamicDestinationResolver() {

  override fun resolveDestinationName(session: Session?, destinationName: String, pubSubDomain: Boolean): Destination {
    val destination = sqsProperties.enabledQueues[destinationName]?.queueName ?: destinationName
    return super.resolveDestinationName(session, destination, pubSubDomain)
  }

  override fun resolveQueue(session: Session, queueName: String): Queue {
    sqsProperties.enabledQueues.filter { it.value.queueName == queueName }.map { it.value }.firstOrNull()?.let {
      if (it.awsAccountId.isNotEmpty() && session is SQSSession) {
        return session.createQueue(queueName, it.awsAccountId)
      }
    }
    return session.createQueue(queueName)
  }
}
