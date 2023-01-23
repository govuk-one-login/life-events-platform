create table event_api_audit
(
    id                 UUID PRIMARY KEY         NOT NULL DEFAULT gen_random_uuid(),
    oauth_client_id    VARCHAR(50)              NOT NULL,
    url                TEXT                     NOT NULL,
    request_method     VARCHAR(16)              NOT NULL,
    payload            JSONB                    NOT NULL,
    when_created       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);
