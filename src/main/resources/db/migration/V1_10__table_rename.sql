ALTER TABLE event_consumer
    RENAME TO consumer;

ALTER TABLE event_publisher
    RENAME TO publisher;

ALTER TABLE event_subscription
    RENAME TO publisher_subscription;