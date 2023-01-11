ALTER TABLE consumer_subscription
    DROP COLUMN is_legacy;

ALTER TABLE consumer_subscription
    DROP COLUMN nino_required;

ALTER TABLE consumer_subscription
    DROP COLUMN last_poll_event_time;


ALTER TABLE consumer_subscription
    ADD COLUMN oauth_client_id VARCHAR(50);

UPDATE consumer_subscription set oauth_client_id = poll_client_id WHERE poll_client_id is NOT NULL;

UPDATE consumer_subscription set oauth_client_id = callback_client_id WHERE callback_client_id is NOT NULL and oauth_client_id is NULL;

ALTER TABLE consumer_subscription
    DROP COLUMN poll_client_id;

ALTER TABLE consumer_subscription
    DROP COLUMN callback_client_id;
