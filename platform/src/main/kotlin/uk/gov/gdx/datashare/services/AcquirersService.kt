package uk.gov.gdx.datashare.services

import com.amazonaws.xray.spring.aop.XRayEnabled
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.gdx.datashare.config.AcquirerNotFoundException
import uk.gov.gdx.datashare.config.AcquirerSubscriptionNotFoundException
import uk.gov.gdx.datashare.config.DateTimeHandler
import uk.gov.gdx.datashare.config.EnrichmentFieldsNotValidForEventTypeException
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.enums.EventTypeEnrichmentFieldsRelationship
import uk.gov.gdx.datashare.models.AcquirerRequest
import uk.gov.gdx.datashare.models.AcquirerSubRequest
import uk.gov.gdx.datashare.models.AcquirerSubscriptionDto
import uk.gov.gdx.datashare.repositories.*
import java.util.*

@Service
@Transactional
@XRayEnabled
class AcquirersService(
  private val acquirerEventRepository: AcquirerEventRepository,
  private val acquirerRepository: AcquirerRepository,
  private val acquirerSubscriptionRepository: AcquirerSubscriptionRepository,
  private val acquirerSubscriptionEnrichmentFieldRepository: AcquirerSubscriptionEnrichmentFieldRepository,
  private val adminActionAlertsService: AdminActionAlertsService,
  private val cognitoService: CognitoService,
  private val dateTimeHandler: DateTimeHandler,
  private val outboundEventQueueService: OutboundEventQueueService,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getAcquirers(): Iterable<Acquirer> = acquirerRepository.findAllByWhenDeletedIsNull()

  fun getAcquirerSubscriptions(): List<AcquirerSubscriptionDto> {
    val acquirerSubscriptions = acquirerSubscriptionRepository.findAllByWhenDeletedIsNull()
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
    queueUrl: String? = null,
  ): AcquirerSubscriptionDto {
    return AcquirerSubscriptionDto(
      acquirerSubscriptionId = acquirerSubscription.acquirerSubscriptionId,
      acquirerId = acquirerSubscription.acquirerId,
      oauthClientId = acquirerSubscription.oauthClientId,
      eventType = acquirerSubscription.eventType,
      enrichmentFields = enrichmentFields.map { it.enrichmentField },
      enrichmentFieldsIncludedInPoll = acquirerSubscription.enrichmentFieldsIncludedInPoll,
      queueName = acquirerSubscription.queueName,
      queueUrl = queueUrl,
      whenCreated = acquirerSubscription.whenCreated,
    )
  }

  private fun addAcquirerSubscriptionEnrichmentFields(
    acquirerSubscriptionId: UUID,
    eventType: EventType,
    enrichmentFields: List<EnrichmentField>,
  ): List<AcquirerSubscriptionEnrichmentField> {
    val eventTypeEnrichmentFields = EventTypeEnrichmentFieldsRelationship[eventType]
    val invalidEnrichmentFields = mutableListOf<EnrichmentField>()
    enrichmentFields.forEach {
      if (!eventTypeEnrichmentFields!!.contains(it)) {
        invalidEnrichmentFields.add(it)
      }
    }
    if (invalidEnrichmentFields.isNotEmpty()) {
      throw EnrichmentFieldsNotValidForEventTypeException("Enrichment fields, $invalidEnrichmentFields, are not valid for the event type $eventType")
    }
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
    adminActionAlertsService.noticeAction(
      AdminAction(
        "Add acquirer subscription",
        object {
          val acquirerId = acquirerId
          val acquirerSubRequest = acquirerSubRequest
        },
      ),
    )
    with(acquirerSubRequest) {
      val acquirerSubscription = acquirerSubscriptionRepository.save(
        AcquirerSubscription(
          acquirerId = acquirerId,
          oauthClientId = oauthClientId,
          eventType = eventType,
          enrichmentFieldsIncludedInPoll = enrichmentFieldsIncludedInPoll,
          queueName = queueName,
        ),
      )
      val enrichmentFields = addAcquirerSubscriptionEnrichmentFields(
        acquirerSubscription.acquirerSubscriptionId,
        acquirerSubscription.eventType,
        enrichmentFields,
      )

      if (queueName != null && principalArn != null) {
        val queueUrl = outboundEventQueueService.createAcquirerQueue(queueName, principalArn)
        return mapAcquirerSubscriptionDto(acquirerSubscription, enrichmentFields, queueUrl)
      }

      return mapAcquirerSubscriptionDto(acquirerSubscription, enrichmentFields)
    }
  }

  fun updateAcquirerSubscription(
    acquirerId: UUID,
    subscriptionId: UUID,
    acquirerSubRequest: AcquirerSubRequest,
  ): AcquirerSubscriptionDto {
    adminActionAlertsService.noticeAction(
      AdminAction(
        "Update acquirer subscription",
        object {
          val acquirerId = acquirerId
          val subscriptionId = subscriptionId
          val acquirerSubRequest = acquirerSubRequest
        },
      ),
    )
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
        addAcquirerSubscriptionEnrichmentFields(
          acquirerSubscription.acquirerSubscriptionId,
          acquirerSubscription.eventType,
          enrichmentFields,
        )

      return mapAcquirerSubscriptionDto(acquirerSubscription, enrichmentFields)
    }
  }

  fun deleteAcquirerSubscription(subscriptionId: UUID) {
    val now = dateTimeHandler.now()
    adminActionAlertsService.noticeAction(
      AdminAction(
        "Delete acquirer subscription",
        object {
          val subscriptionId = subscriptionId
          val whenDeleted = now
        },
      ),
    )
    val subscription = acquirerSubscriptionRepository.save(
      acquirerSubscriptionRepository.findByIdOrNull(subscriptionId)?.copy(
        whenDeleted = now,
      ) ?: throw AcquirerSubscriptionNotFoundException("Subscription $subscriptionId not found"),
    )

    acquirerSubscriptionEnrichmentFieldRepository
      .deleteAllByAcquirerSubscriptionId(subscriptionId)

    acquirerEventRepository.softDeleteAllByAcquirerSubscriptionId(subscriptionId, now)

    if (subscription.queueName == null && subscription.oauthClientId == null) {
      log.warn("Acquirer does not have a client id or queue name.")
      return
    }

    if (subscription.oauthClientId != null) {
      val otherSubscriptionsWithClient =
        acquirerSubscriptionRepository.findAllByOauthClientIdAndWhenDeletedIsNull(subscription.oauthClientId)
      if (otherSubscriptionsWithClient.isEmpty()) {
        log.info("Deleting User Pool Client ID: ${subscription.oauthClientId}")
        cognitoService.deleteUserPoolClient(subscription.oauthClientId)
      }
    }

    if (subscription.queueName != null) {
      val otherSubscriptionsOnQueue =
        acquirerSubscriptionRepository.findAllByQueueNameAndWhenDeletedIsNull(subscription.queueName)
      if (otherSubscriptionsOnQueue.isEmpty()) {
        outboundEventQueueService.deleteAcquirerQueueAndDlq(subscription.queueName)
      }
    }
  }

  fun addAcquirer(
    acquirerRequest: AcquirerRequest,
  ): Acquirer {
    adminActionAlertsService.noticeAction(AdminAction("Add acquirer", acquirerRequest))
    with(acquirerRequest) {
      return acquirerRepository.save(
        Acquirer(
          name = name,
        ),
      )
    }
  }

  fun deleteAcquirer(id: UUID) {
    val now = dateTimeHandler.now()
    adminActionAlertsService.noticeAction(
      AdminAction(
        "Delete acquirer",
        object {
          val acquirerId = id
          val whenDeleted = now
        },
      ),
    )
    acquirerRepository.save(
      acquirerRepository.findByIdOrNull(id)?.copy(
        whenDeleted = now,
      ) ?: throw AcquirerNotFoundException("Acquirer $id not found"),
    )
    val subscriptions = acquirerSubscriptionRepository.findAllByAcquirerId(id)
    subscriptions.forEach { deleteAcquirerSubscription(it.id) }
  }

  fun getEnrichmentFieldsForAcquirerSubscription(acquirerSubscription: AcquirerSubscription): List<EnrichmentField> {
    return acquirerSubscriptionEnrichmentFieldRepository.findAllByAcquirerSubscriptionId(acquirerSubscription.id)
      .map { it.enrichmentField }
  }
}
