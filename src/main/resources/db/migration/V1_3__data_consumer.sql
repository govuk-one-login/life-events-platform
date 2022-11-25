create table data_consumer
(
    client_id           varchar(60)              NOT NULL PRIMARY KEY,
    client_name         varchar(80)              NOT NULL,
    allowed_event_types varchar(200)             NOT NULL,
    nino_required       boolean                  NOT NULL default false,
    other_data_sets     varchar(200),
    when_created        timestamp with time zone not null default now()

);

insert into data_consumer (client_id, client_name, allowed_event_types, nino_required)
values ('2ippgk5eigcllh62oi1v35q9bp', 'Other Gov Dept 1 (High Tech)', 'DEATH_NOTIFICATION,BIRTH_NOTIFICATION', true);

insert into data_consumer (client_id, client_name, allowed_event_types, nino_required)
values ('6obo37soq65d0iv301k0ul1cob', 'Other Gov Dept 2 (API only)', 'DEATH_NOTIFICATION', false);

insert into data_consumer (client_id, client_name, allowed_event_types, nino_required)
values ('73adfkd9b2gg45nchhrj6tob6r', 'Internal Outbound Adaptor', 'DEATH_NOTIFICATION', true);

