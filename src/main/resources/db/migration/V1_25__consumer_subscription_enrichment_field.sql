create table consumer_subscription_enrichment_field
(
    id                       UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    consumer_subscription_id UUID             NOT NULL,
    enrichment_field         VARCHAR(200)     NOT NULL,

    CONSTRAINT fk_consumer_subscription
        FOREIGN KEY (consumer_subscription_id)
            REFERENCES consumer_subscription (id)
);

INSERT INTO consumer_subscription_enrichment_field (consumer_subscription_id, enrichment_field)
SELECT id, unnest(string_to_array(enrichment_fields::TEXT, ','))
FROM consumer_subscription;

ALTER TABLE consumer_subscription
    DROP COLUMN enrichment_fields;
