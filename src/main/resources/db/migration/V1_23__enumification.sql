ALTER TABLE consumer_subscription
    DROP CONSTRAINT fk_ingress_event_type;

ALTER TABLE publisher_subscription
    DROP CONSTRAINT fk_event_type;

ALTER TABLE publisher_subscription
    DROP CONSTRAINT fk_dataset;

ALTER TABLE publisher_subscription
    DROP COLUMN dataset_id;

ALTER TABLE event_data
    DROP COLUMN dataset_id;


DROP TABLE event_type;

DROP TABLE event_dataset;

ALTER TABLE publisher_subscription
    RENAME COLUMN event_type_id TO event_type
