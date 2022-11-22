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
values ('6cobo7t77hpgd8t8c7pqb1dlt4', 'Other Gov Dept 1 (High Tech)', 'DEATH_NOTIFICATION,BIRTH_NOTIFICATION', true);

insert into data_consumer (client_id, client_name, allowed_event_types, nino_required)
values ('fff2', 'Other Gov Dept 2 (API only)', 'DEATH_NOTIFICATION', false);

insert into data_consumer (client_id, client_name, allowed_event_types, nino_required)
values ('fff3', 'Other Gov Dept 3 (Low Tech)', 'DEATH_NOTIFICATION', true);
