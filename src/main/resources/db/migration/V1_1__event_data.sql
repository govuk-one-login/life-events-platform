create table event_data
(
    event_id         varchar(39) PRIMARY KEY,
    event_type       varchar(50)              NOT NULL,
    data_provider    varchar(80)              NOT NULL,
    dataset_type     varchar(30)              NOT NULL,
    data_id          varchar(80)              NOT NULL,
    data_payload     text                     NULL,
    when_created     timestamp with time zone not null default now(),
    data_expiry_time timestamp with time zone not null
);
