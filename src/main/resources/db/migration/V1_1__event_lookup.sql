create table event_lookup
(
    event_id        varchar(36)  PRIMARY KEY,
    lev_lookup_id   bigint NOT NULL,
    event_type      varchar(50) NOT NULL,
    when_created    timestamp with time zone not null default now()
);
