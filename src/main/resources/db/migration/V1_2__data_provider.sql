create table data_provider
(
    client_id     varchar(60)              NOT NULL PRIMARY KEY,
    client_name   varchar(80)              NOT NULL,
    event_type    varchar(30)              NOT NULL,
    dataset_type  varchar(30)              NOT NULL,
    store_payload boolean                  NOT NULL DEFAULT false,
    when_created  timestamp with time zone not null default now()
);


insert into data_provider (client_id, client_name, event_type, dataset_type)
values ('2koca5i6ct15orqh3g004pmvre', 'HMPO', 'DEATH_NOTIFICATION', 'DEATH_LEV');

insert into data_provider (client_id, client_name, event_type, dataset_type, store_payload)
values ('7audlmkc3fujbu0uuq7u3vnsp3', 'DWP', 'DEATH_NOTIFICATION', 'DEATH_CSV', true);

insert into data_provider (client_id, client_name, event_type, dataset_type, store_payload)
values ('2mesjc8s8vb496t325f2eojaha', 'Internal Inbound Adaptor', 'DEATH_NOTIFICATION', 'DEATH_CSV', true);