CREATE TABLE supplier
(
    id           UUID PRIMARY KEY         NOT NULL DEFAULT gen_random_uuid(),
    name         VARCHAR(80)              NOT NULL UNIQUE,
    when_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE supplier_subscription
(
    id           UUID PRIMARY KEY         NOT NULL DEFAULT gen_random_uuid(),
    supplier_id  UUID                     NOT NULL,
    client_id    VARCHAR(50)              NOT NULL,
    event_type   VARCHAR(50)              NOT NULL,
    when_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT uk_event_type_client_id UNIQUE (event_type, client_id),

    CONSTRAINT fk_supplier
        FOREIGN KEY (supplier_id)
            REFERENCES supplier (id)
);

CREATE TABLE acquirer
(
    id           UUID PRIMARY KEY         NOT NULL DEFAULT gen_random_uuid(),
    name         VARCHAR(80)              NOT NULL UNIQUE,
    when_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);


CREATE TABLE acquirer_subscription
(
    id                                 UUID PRIMARY KEY         NOT NULL DEFAULT gen_random_uuid(),
    acquirer_id                        UUID                     NOT NULL,
    when_created                       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    event_type                         VARCHAR(50)              NOT NULL,
    oauth_client_id                    VARCHAR(50),
    enrichment_fields_included_in_poll BOOLEAN                  NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_acquirer
        FOREIGN KEY (acquirer_id)
            REFERENCES acquirer (id)
);

CREATE TABLE event_data
(
    id                       UUID PRIMARY KEY         NOT NULL DEFAULT gen_random_uuid(),
    acquirer_subscription_id UUID                     NOT NULL,
    data_id                  VARCHAR(80)              NOT NULL,
    when_created             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    event_time               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    deleted_at               TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_acquirer_subscription
        FOREIGN KEY (acquirer_subscription_id)
            REFERENCES acquirer_subscription
);

CREATE TABLE shedlock
(
    name       VARCHAR(64) PRIMARY KEY NOT NULL,
    lock_until TIMESTAMP               NOT NULL,
    locked_at  TIMESTAMP               NOT NULL,
    locked_by  VARCHAR(255)            NOT NULL
);

CREATE TABLE event_api_audit
(
    id              UUID PRIMARY KEY         NOT NULL DEFAULT gen_random_uuid(),
    oauth_client_id VARCHAR(50)              NOT NULL,
    url             TEXT                     NOT NULL,
    request_method  VARCHAR(16)              NOT NULL,
    payload         JSONB                    NOT NULL,
    when_created    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE acquirer_subscription_enrichment_field
(
    id                       UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    acquirer_subscription_id UUID             NOT NULL,
    enrichment_field         VARCHAR(200)     NOT NULL,

    CONSTRAINT fk_acquirer_subscription
        FOREIGN KEY (acquirer_subscription_id)
            REFERENCES acquirer_subscription (id)
);
