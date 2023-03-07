package uk.gov.gdx.datashare.services

import com.amazonaws.xray.spring.aop.XRayEnabled
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.gdx.datashare.repositories.AcquirerEvent
import uk.gov.gdx.datashare.repositories.AcquirerEventRepository

@Service
@XRayEnabled
class AcquirerEventProcessor(
  private val objectMapper: ObjectMapper,
  private val acquirerEventRepository: AcquirerEventRepository,
) {

  @JmsListener(destination = "acquirerevent", containerFactory = "awsQueueContainerFactoryProxy")
  @Transactional
  fun onAcquirerEvent(message: String) {
    val acquirerEvent = objectMapper.readValue(message, AcquirerEvent::class.java)
    persistEvent(acquirerEvent)
  }

  private fun persistEvent(acquirerEvent: AcquirerEvent) {
    acquirerEventRepository.save(acquirerEvent)
  }
}
