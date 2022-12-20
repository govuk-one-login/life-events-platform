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


create table publisher_subscription
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

insert into publisher_subscription (client_id, publisher_id, event_type_id, dataset_id)
select 'len', id, 'DEATH_NOTIFICATION', 'DEATH_LEV'
from event_publisher
WHERE publisher_name = 'HMPO';

insert into publisher_subscription (client_id, publisher_id, event_type_id, dataset_id)
values ('internal-inbound', (select id from event_publisher WHERE publisher_name = 'HMPO'),
        'LIFE_EVENT',
        'PASS_THROUGH');

insert into publisher_subscription (client_id, publisher_id, event_type_id, dataset_id)
values ('internal-inbound', (select id from event_publisher WHERE publisher_name = 'HMPO'),
        'DEATH_NOTIFICATION',
        'DEATH_CSV');

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
            REFERENCES publisher_subscription (id)
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

insert into consumer_subscription (poll_client_id, callback_client_id, consumer_id, event_type_id, nino_required)
values ('dwp-event-receiver', 'dwp-event-receiver', (select id from event_consumer WHERE consumer_name = 'DWP Poller'),
        'DEATH_NOTIFICATION', true);
insert into consumer_subscription (poll_client_id, callback_client_id, consumer_id, event_type_id)
values ('dwp-event-receiver', 'dwp-event-receiver', (select id from event_consumer WHERE consumer_name = 'DWP Poller'),
        'LIFE_EVENT');

insert into consumer_subscription (callback_client_id, consumer_id, event_type_id, nino_required)
values ('hmrc-client', (select id from event_consumer WHERE consumer_name = 'Pub/Sub Consumer'),
        'DEATH_NOTIFICATION', true);
insert into consumer_subscription (callback_client_id, consumer_id, event_type_id)
values ('hmrc-client', (select id from event_consumer WHERE consumer_name = 'Pub/Sub Consumer'),
        'LIFE_EVENT');

insert into consumer_subscription (callback_client_id, consumer_id, event_type_id)
values ('internal-outbound', (select id from event_consumer WHERE consumer_name = 'Internal Adaptor'),
        'LIFE_EVENT');
insert into consumer_subscription (callback_client_id, consumer_id, event_type_id, nino_required)
values ('internal-outbound', (select id from event_consumer WHERE consumer_name = 'Internal Adaptor'),
        'DEATH_NOTIFICATION', true);

insert into consumer_subscription (consumer_id, event_type_id, push_uri, nino_required)
values ((select id from event_consumer WHERE consumer_name = 'S3 Consumer'),
        'DEATH_NOTIFICATION',
        's3://user:password@localhost', true);

insert into consumer_subscription (consumer_id, event_type_id, push_uri)
values ((select id from event_consumer WHERE consumer_name = 'S3 Consumer'),
        'LIFE_EVENT',
        's3://user:password@localhost');

insert into consumer_subscription (consumer_id, event_type_id, push_uri)
values ((select id from event_consumer WHERE consumer_name = 'Webhook Consumer'),
        'LIFE_EVENT',
        'http://localhost:8181/callback');
