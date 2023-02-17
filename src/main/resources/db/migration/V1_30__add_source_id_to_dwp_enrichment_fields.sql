INSERT INTO acquirer_subscription_enrichment_field (acquirer_subscription_id, enrichment_field)
SELECT asub.id, 'SOURCE_ID'
FROM acquirer_subscription asub join acquirer a on asub.acquirer_id = a.id
WHERE asub.event_type = 'DEATH_NOTIFICATION' and a.name like '%DWP%' and not exists (
        select 1 from acquirer_subscription_enrichment_field asube where asube.acquirer_subscription_id = asub.id and asube.enrichment_field = 'SOURCE_ID'
    );
