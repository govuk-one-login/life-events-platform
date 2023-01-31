package uk.gov.gdx.datashare.services

import com.amazonaws.xray.spring.aop.XRayEnabled
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.gdx.datashare.config.AcquirerSubscriptionNotFoundException
import uk.gov.gdx.datashare.models.AcquirerRequest
import uk.gov.gdx.datashare.models.AcquirerSubRequest
import uk.gov.gdx.datashare.models.AcquirerSubscriptionDto
import uk.gov.gdx.datashare.repositories.*
import java.util.*

@Service
@Transactional
@XRayEnabled
class AcquirersService(
  private val acquirerSubscriptionRepository: AcquirerSubscriptionRepository,
  private val acquirerRepository: AcquirerRepository,
  private val acquirerSubscriptionEnrichmentFieldRepository: AcquirerSubscriptionEnrichmentFieldRepository,
) {
  fun getAcquirers() = acquirerRepository.findAll()

  fun getAcquirerSubscriptions(): List<AcquirerSubscriptionDto> {
    val acquirerSubscriptions = acquirerSubscriptionRepository.findAll()
    return acquirerSubscriptions.map {
      val enrichmentFields =
        acquirerSubscriptionEnrichmentFieldRepository.findAllByAcquirerSubscriptionId(it.acquirerSubscriptionId)
      mapAcquirerSubscriptionDto(it, enrichmentFields)
    }
  }

  fun getSubscriptionsForAcquirer(acquirerId: UUID): List<AcquirerSubscriptionDto> {
    val acquirerSubscriptions = acquirerSubscriptionRepository.findAllByAcquirerId(acquirerId)
    return acquirerSubscriptions.map {
      val enrichmentFields =
        acquirerSubscriptionEnrichmentFieldRepository.findAllByAcquirerSubscriptionId(it.acquirerSubscriptionId)
      mapAcquirerSubscriptionDto(it, enrichmentFields)
    }
  }

  private fun mapAcquirerSubscriptionDto(
    acquirerSubscription: AcquirerSubscription,
    enrichmentFields: List<AcquirerSubscriptionEnrichmentField>,
  ): AcquirerSubscriptionDto {
    return AcquirerSubscriptionDto(
      acquirerSubscriptionId = acquirerSubscription.acquirerSubscriptionId,
      acquirerId = acquirerSubscription.acquirerId,
      oauthClientId = acquirerSubscription.oauthClientId,
      eventType = acquirerSubscription.eventType,
      enrichmentFields = enrichmentFields.map { it.enrichmentField },
      enrichmentFieldsIncludedInPoll = acquirerSubscription.enrichmentFieldsIncludedInPoll,
      whenCreated = acquirerSubscription.whenCreated,
    )
  }

  private fun addAcquirerSubscriptionEnrichmentFields(
    acquirerSubscriptionId: UUID,
    enrichmentFields: List<String>,
  ): List<AcquirerSubscriptionEnrichmentField> {
    return acquirerSubscriptionEnrichmentFieldRepository.saveAll(
      enrichmentFields.map {
        AcquirerSubscriptionEnrichmentField(
          acquirerSubscriptionId = acquirerSubscriptionId,
          enrichmentField = it,
        )
      },
    ).toList()
  }

  fun addAcquirerSubscription(
    acquirerId: UUID,
    acquirerSubRequest: AcquirerSubRequest,
  ): AcquirerSubscriptionDto {
    with(acquirerSubRequest) {
      val acquirerSubscription = acquirerSubscriptionRepository.save(
        AcquirerSubscription(
          acquirerId = acquirerId,
          oauthClientId = oauthClientId,
          eventType = eventType,
        ),
      )
      val enrichmentFields =
        addAcquirerSubscriptionEnrichmentFields(acquirerSubscription.acquirerSubscriptionId, enrichmentFields)

      return mapAcquirerSubscriptionDto(acquirerSubscription, enrichmentFields)
    }
  }

  fun updateAcquirerSubscription(
    acquirerId: UUID,
    subscriptionId: UUID,
    acquirerSubRequest: AcquirerSubRequest,
  ): AcquirerSubscriptionDto {
    with(acquirerSubRequest) {
      val acquirerSubscription = acquirerSubscriptionRepository.save(
        acquirerSubscriptionRepository.findByIdOrNull(subscriptionId)?.copy(
          acquirerId = acquirerId,
          oauthClientId = oauthClientId,
          eventType = eventType,
          enrichmentFieldsIncludedInPoll = enrichmentFieldsIncludedInPoll,
        ) ?: throw AcquirerSubscriptionNotFoundException("Subscription $subscriptionId not found"),
      )

      acquirerSubscriptionEnrichmentFieldRepository.deleteAllByAcquirerSubscriptionId(acquirerSubscription.acquirerSubscriptionId)
      val enrichmentFields =
        addAcquirerSubscriptionEnrichmentFields(acquirerSubscription.acquirerSubscriptionId, enrichmentFields)

      return mapAcquirerSubscriptionDto(acquirerSubscription, enrichmentFields)
    }
  }

  fun addAcquirer(
    acquirerRequest: AcquirerRequest,
  ): Acquirer {
    with(acquirerRequest) {
      return acquirerRepository.save(
        Acquirer(
          name = name,
        ),
      )
    }
  }
}
