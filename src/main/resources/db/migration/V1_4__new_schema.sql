drop table data_consumer;
drop table data_provider;
drop table event_data;


create table event_type
(
    id           varchar(50)              NOT NULL PRIMARY KEY,
    description  varchar(200)             NOT NULL,
    active       boolean                  NOT NULL default true,
    when_created timestamp with time zone not null default now()
);


insert into event_type (id, description)
values ('DEATH_NOTIFICATION', 'Death has been registered with the GRO');
insert into event_type (id, description)
values ('LIFE_EVENT', 'Another life event (test)');

create table event_dataset
(
    id            varchar(50)              NOT NULL PRIMARY KEY,
    description   varchar(200)             NOT NULL,
    store_payload boolean                  NOT NULL DEFAULT false,
    active        boolean                  NOT NULL default true,
    when_created  timestamp with time zone not null default now()
);

insert into event_dataset (id, description)
values ('DEATH_LEV', 'Core Death schema');
insert into event_dataset (id, description, store_payload)
values ('DEATH_CSV', 'Core Death schema in CSV format', true);
insert into event_dataset (id, description, store_payload)
values ('PASS_THROUGH', 'Just pass the data through', true);

create table event_publisher
(
    id             SERIAL PRIMARY KEY       NOT NULL,
    publisher_name varchar(80)              NOT NULL UNIQUE,
    when_created   timestamp with time zone not null default now()
);


insert into event_publisher (publisher_name)
values ('HMPO');


create table event_subscription
(
    id            SERIAL PRIMARY KEY       NOT NULL,
    publisher_id  BIGINT                   NOT NULL,
    client_id     VARCHAR(50)              NOT NULL,
    event_type_id VARCHAR(50)              NOT NULL,
    dataset_id    VARCHAR(50)              NOT NULL,
    when_created  timestamp with time zone not null default now(),

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
    event_id         varchar(39)              NOT NULL,
    event_type_id    varchar(50)              NOT NULL,
    dataset_id       VARCHAR(50)              NOT NULL,
    subscription_id  BIGINT                   NOT NULL,
    data_id          varchar(80)              NOT NULL,
    data_payload     text                     NULL,
    when_created     timestamp with time zone not null default now(),
    data_expiry_time timestamp with time zone not null,
    PRIMARY KEY (event_id),
    CONSTRAINT fk_event_type
        FOREIGN KEY (event_type_id)
            REFERENCES event_type (id),

    CONSTRAINT fk_subscription
        FOREIGN KEY (subscription_id)
            REFERENCES event_subscription (id)
);



create table event_consumer
(
    id            SERIAL PRIMARY KEY       NOT NULL,
    consumer_name varchar(80)              NOT NULL UNIQUE,
    when_created  timestamp with time zone not null default now()
);

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

create table consumer_subscription
(
    id                   SERIAL PRIMARY KEY       NOT NULL,
    consumer_id          BIGINT                   NOT NULL,
    event_type_id        VARCHAR(50)              NOT NULL,
    poll_client_id       VARCHAR(50),
    callback_client_id   VARCHAR(50),
    last_poll_event_time timestamp with time zone,
    push_uri             varchar(400),
    nino_required        boolean                  not null default false,
    when_created         timestamp with time zone not null default now(),

    CONSTRAINT fk_event_type
        FOREIGN KEY (event_type_id)
            REFERENCES event_type (id),

    CONSTRAINT fk_consumer
        FOREIGN KEY (consumer_id)
            REFERENCES event_consumer (id),

    CONSTRAINT uk_event_client UNIQUE (event_type_id, callback_client_id)
);
