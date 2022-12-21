alter table event_data
rename to ingress_event_data;

alter table event_type
rename to ingress_event_type;

drop table consumer_subscription;


create table egress_event_type
(
    id                 UUID PRIMARY KEY         NOT NULL DEFAULT gen_random_uuid(),
    ingress_event_type VARCHAR(50)              NOT NULL,
    description        VARCHAR(200)             NOT NULL,
    active             BOOLEAN                  NOT NULL DEFAULT true,
    when_created       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT fk_ingress_event_type
        FOREIGN KEY (ingress_event_type)
            REFERENCES ingress_event_type (id)
);

create table egress_event_data
(
    id               UUID PRIMARY KEY         NOT NULL DEFAULT gen_random_uuid(),
    type_id          UUID                     NOT NULL,
    ingress_event_id UUID                     NOT NULL,
    dataset_id       VARCHAR(50)              NOT NULL,
    data_id          VARCHAR(80)              NOT NULL,
    data_payload     TEXT                     NULL,
    data_expiry_time TIMESTAMP WITH TIME ZONE NOT NULL,
    when_created     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT fk_ingress_event_data
        FOREIGN KEY (ingress_event_id)
            REFERENCES ingress_event_data (id),

    CONSTRAINT fk_event_type
        FOREIGN KEY (type_id)
            REFERENCES egress_event_type (id)
);

create table consumer_subscription
(
    id                   UUID PRIMARY KEY         NOT NULL DEFAULT gen_random_uuid(),
    consumer_id          UUID                     NOT NULL,
    event_type_id        UUID                     NOT NULL,
    poll_client_id       VARCHAR(50)              NULL,
    callback_client_id   VARCHAR(50)              NULL,
    last_poll_event_time TIMESTAMP WITH TIME ZONE NULL,
    push_uri             VARCHAR(400)             NULL,
    nino_required        BOOLEAN                  NOT NULL DEFAULT false,
    when_created         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT fk_event_type
        FOREIGN KEY (event_type_id)
            REFERENCES egress_event_type (id),

    CONSTRAINT fk_consumer
        FOREIGN KEY (consumer_id)
            REFERENCES event_consumer (id),

    CONSTRAINT uk_event_client UNIQUE (event_type_id, callback_client_id)
);