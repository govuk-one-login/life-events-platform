ALTER TABLE publisher_subscription
    RENAME COLUMN publisher_id to supplier_id;

ALTER TABLE publisher
    RENAME CONSTRAINT event_publisher_pkey TO supplier_pkey;
ALTER TABLE publisher
    RENAME CONSTRAINT event_publisher_publisher_name_key TO supplier_name_key;

ALTER TABLE publisher_subscription
    RENAME CONSTRAINT event_subscription_pkey TO supplier_subscription_pkey;
ALTER TABLE publisher_subscription
    RENAME CONSTRAINT uk_pub_client TO uk_event_type_client_id;

ALTER TABLE publisher
    RENAME TO supplier;

ALTER TABLE publisher_subscription
    RENAME TO supplier_subscription;
