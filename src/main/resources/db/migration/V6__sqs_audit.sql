ALTER TABLE acquirer_event_audit
    ALTER oauth_client_id DROP NOT NULL,
    ALTER url DROP NOT NULL,
    ALTER request_method DROP NOT NULL,
    ADD queue_name VARCHAR(80);
