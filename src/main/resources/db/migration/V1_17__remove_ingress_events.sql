ALTER TABLE egress_event_data
    DROP CONSTRAINT fk_ingress_event_data;

ALTER TABLE egress_event_data
    DROP COLUMN ingress_event_id;

ALTER TABLE egress_event_data
    RENAME TO event_data;

ALTER TABLE ingress_event_type
    RENAME TO event_type;

ALTER TABLE consumer_subscription
    RENAME COLUMN ingress_event_type TO event_type;

DROP TABLE ingress_event_data;
