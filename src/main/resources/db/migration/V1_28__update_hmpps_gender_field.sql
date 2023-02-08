UPDATE acquirer_subscription_enrichment_field
SET enrichment_field='GENDER'
FROM acquirer_subscription
WHERE acquirer_subscription.id = acquirer_subscription_enrichment_field.acquirer_subscription_id
  AND enrichment_field = 'SEX'
  AND acquirer_subscription.event_type = 'ENTERED_PRISON';
