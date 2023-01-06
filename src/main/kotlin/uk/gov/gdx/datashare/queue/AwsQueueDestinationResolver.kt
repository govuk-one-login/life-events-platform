package uk.gov.gdx.datashare.queue

import org.springframework.jms.support.destination.DynamicDestinationResolver
import javax.jms.Destination
import javax.jms.Session

class AwsQueueDestinationResolver(private val sqsProperties: SqsProperties) : DynamicDestinationResolver() {

  override fun resolveDestinationName(session: Session?, destinationName: String, pubSubDomain: Boolean): Destination {
    val destination = sqsProperties.queues[destinationName]?.queueName ?: destinationName
    return super.resolveDestinationName(session, destination, pubSubDomain)
  }
}
