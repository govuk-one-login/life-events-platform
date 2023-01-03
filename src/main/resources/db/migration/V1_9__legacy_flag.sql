ALTER TABLE consumer_subscription
    ADD COLUMN is_legacy BOOLEAN NOT NULL DEFAULT false;