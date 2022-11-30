create table data_consumer
(
    client_id            varchar(60)              NOT NULL PRIMARY KEY,
    client_name          varchar(80)              NOT NULL,
    allowed_event_types  varchar(200)             NOT NULL,
    last_poll_event_time timestamp with time zone,
    nino_required        boolean                  NOT NULL default false,
    other_data_sets      varchar(200),
    when_created         timestamp with time zone not null default now()

);

insert into data_consumer (client_id, client_name, allowed_event_types, nino_required)
values ('dwp-event-receiver', 'DWP', 'DEATH_NOTIFICATION,BIRTH_NOTIFICATION', true);

insert into data_consumer (client_id, client_name, allowed_event_types, nino_required)
values ('hmrc-client', 'HMRC', 'DEATH_NOTIFICATION', false);

insert into data_consumer (client_id, client_name, allowed_event_types, nino_required)
values ('internal-outbound', 'Internal Outbound Adaptor', 'DEATH_NOTIFICATION', true);

