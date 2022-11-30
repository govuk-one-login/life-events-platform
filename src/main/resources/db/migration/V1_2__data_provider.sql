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
values ('len', 'HMPO', 'DEATH_NOTIFICATION', 'DEATH_LEV');

insert into data_provider (client_id, client_name, event_type, dataset_type, store_payload)
values ('internal-inbound', 'Internal Inbound Adaptor', 'DEATH_NOTIFICATION', 'DEATH_CSV', true);