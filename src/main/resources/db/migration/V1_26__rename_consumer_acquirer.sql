ALTER TABLE consumer_subscription
    RENAME COLUMN consumer_id TO acquirer_id;

ALTER TABLE consumer_subscription_enrichment_field
    RENAME COLUMN consumer_subscription_id TO acquirer_subscription_id;

ALTER TABLE event_data
    RENAME COLUMN consumer_subscription_id TO acquirer_subscription_id;

ALTER TABLE consumer
    RENAME CONSTRAINT event_consumer_pkey TO acquirer_pkey;
ALTER TABLE consumer
    RENAME CONSTRAINT event_consumer_consumer_name_key TO acquirer_name_key;

ALTER TABLE consumer_subscription
    RENAME CONSTRAINT consumer_subscription_pkey TO acquirer_subscription_pkey;
ALTER TABLE consumer_subscription
    RENAME CONSTRAINT fk_consumer TO fk_acquirer;

ALTER TABLE consumer_subscription_enrichment_field
    RENAME CONSTRAINT consumer_subscription_enrichment_field_pkey TO acquirer_subscription_enrichment_field_pkey;
ALTER TABLE consumer_subscription_enrichment_field
    RENAME CONSTRAINT fk_consumer_subscription TO fk_acquirer_subscription;

ALTER TABLE event_data
    RENAME CONSTRAINT egress_event_data_pkey TO event_data_pkey;
ALTER TABLE event_data
    RENAME CONSTRAINT fk_consumer_subscription TO fk_acquirer_subscription;

ALTER TABLE consumer RENAME TO acquirer;
ALTER TABLE consumer_subscription RENAME TO acquirer_subscription;
ALTER TABLE consumer_subscription_enrichment_field RENAME TO acquirer_subscription_enrichment_field;
