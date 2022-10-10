create table event_lookup
(
    id              UUID  PRIMARY KEY,
    len_event_id    bigint NOT NULL,
    lev_lookup_id   bigint NOT NULL,
    when_created    timestamp with time zone not null default now()
);
