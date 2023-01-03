ALTER TABLE consumer_subscription
    ADD COLUMN enrichment_fields TEXT NULL;

ALTER TABLE consumer_subscription
    ADD COLUMN ingress_event_type VARCHAR(50) NULL;

ALTER TABLE egress_event_data
    DROP CONSTRAINT fk_event_type;

ALTER TABLE consumer_subscription
    DROP CONSTRAINT fk_event_type;


UPDATE consumer_subscription
SET enrichment_fields  = eet.enrichment_fields,
    ingress_event_type = eet.ingress_event_type
FROM consumer_subscription cs
    JOIN egress_event_type eet ON cs.event_type_id = eet.id;

DELETE FROM egress_event_data;


ALTER TABLE egress_event_data
    RENAME COLUMN type_id TO consumer_subscription_id;

ALTER TABLE consumer_subscription
    DROP COLUMN event_type_id;


ALTER TABLE consumer_subscription
    ALTER COLUMN enrichment_fields SET NOT NULL;

ALTER TABLE consumer_subscription
    ALTER COLUMN ingress_event_type SET NOT NULL;

ALTER TABLE consumer_subscription
    ADD CONSTRAINT fk_ingress_event_type
        FOREIGN KEY (ingress_event_type)
            REFERENCES ingress_event_type (id);

ALTER TABLE egress_event_data
    ADD CONSTRAINT fk_consumer_subscription
        FOREIGN KEY (consumer_subscription_id)
            REFERENCES consumer_subscription (id);


DROP TABLE egress_event_type;

