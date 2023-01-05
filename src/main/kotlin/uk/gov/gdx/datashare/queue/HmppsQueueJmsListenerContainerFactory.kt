package uk.gov.gdx.datashare.queue

import org.springframework.jms.config.DefaultJmsListenerContainerFactory
import org.springframework.jms.config.JmsListenerEndpoint
import org.springframework.jms.config.MethodJmsListenerEndpoint
import org.springframework.jms.listener.DefaultMessageListenerContainer

class JmsListenerContainerFactoryMissingException(message: String) : RuntimeException(message)

data class HmppsQueueDestinationContainerFactory(
  val destination: String,
  val factory: DefaultJmsListenerContainerFactory
)

class HmppsQueueJmsListenerContainerFactory(private val factories: List<HmppsQueueDestinationContainerFactory>) : DefaultJmsListenerContainerFactory() {
  override fun createListenerContainer(endpoint: JmsListenerEndpoint): DefaultMessageListenerContainer {
    return factories
      .firstOrNull { it.destination == (endpoint as MethodJmsListenerEndpoint).destination }
      ?.factory
      ?.createListenerContainer(endpoint)
      ?: throw JmsListenerContainerFactoryMissingException("Unable to find jms listener container factory for endpoint ${(endpoint as MethodJmsListenerEndpoint).destination}")
  }
}
