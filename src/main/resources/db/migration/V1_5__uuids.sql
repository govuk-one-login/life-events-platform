drop table consumer_subscription;
drop table event_consumer;
drop table event_data;
drop table event_subscription;
drop table event_publisher;


create table event_publisher
(
    id             UUID PRIMARY KEY         NOT NULL DEFAULT gen_random_uuid(),
    publisher_name VARCHAR(80)              NOT NULL UNIQUE,
    when_created   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

create table event_subscription
(
    id            UUID PRIMARY KEY         NOT NULL DEFAULT gen_random_uuid(),
    publisher_id  UUID                     NOT NULL,
    client_id     VARCHAR(50)              NOT NULL,
    event_type_id VARCHAR(50)              NOT NULL,
    dataset_id    VARCHAR(50)              NOT NULL,
    when_created  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT fk_event_type
        FOREIGN KEY (event_type_id)
            REFERENCES event_type (id),

    CONSTRAINT fk_publisher
        FOREIGN KEY (publisher_id)
            REFERENCES event_publisher (id),

    CONSTRAINT fk_dataset
        FOREIGN KEY (dataset_id)
            REFERENCES event_dataset (id),

    CONSTRAINT uk_pub_client UNIQUE (event_type_id, client_id)
);


create table event_data
(
    id               UUID PRIMARY KEY         NOT NULL DEFAULT gen_random_uuid(),
    event_type_id    VARCHAR(50)              NOT NULL,
    dataset_id       VARCHAR(50)              NOT NULL,
    subscription_id  UUID                     NOT NULL,
    data_id          VARCHAR(80)              NOT NULL,
    data_payload     TEXT                     NULL,
    when_created     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    data_expiry_time TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_event_type
        FOREIGN KEY (event_type_id)
            REFERENCES event_type (id),

    CONSTRAINT fk_subscription
        FOREIGN KEY (subscription_id)
            REFERENCES event_subscription (id)
);


create table event_consumer
(
    id            UUID PRIMARY KEY         NOT NULL DEFAULT gen_random_uuid(),
    consumer_name VARCHAR(80)              NOT NULL UNIQUE,
    when_created  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

create table consumer_subscription
(
    id                   UUID PRIMARY KEY         NOT NULL DEFAULT gen_random_uuid(),
    consumer_id          UUID                     NOT NULL,
    event_type_id        VARCHAR(50)              NOT NULL,
    poll_client_id       VARCHAR(50)              NULL,
    callback_client_id   VARCHAR(50)              NULL,
    last_poll_event_time TIMESTAMP WITH TIME ZONE NULL,
    push_uri             VARCHAR(400)             NULL,
    nino_required        BOOLEAN                  NOT NULL DEFAULT false,
    when_created         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT fk_event_type
        FOREIGN KEY (event_type_id)
            REFERENCES event_type (id),

    CONSTRAINT fk_consumer
        FOREIGN KEY (consumer_id)
            REFERENCES event_consumer (id),

    CONSTRAINT uk_event_client UNIQUE (event_type_id, callback_client_id)
);


insert into event_publisher (publisher_name)
values ('HMPO');

insert into event_consumer (consumer_name)
values ('DWP Poller');

insert into event_consumer (consumer_name)
values ('Pub/Sub Consumer');

insert into event_consumer (consumer_name)
values ('S3 Consumer');

insert into event_consumer (consumer_name)
values ('Webhook Consumer');

insert into event_consumer (consumer_name)
values ('Internal Adaptor');