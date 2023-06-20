--region Create supplier event
CREATE TABLE supplier_event
(
    id                       UUID PRIMARY KEY         NOT NULL DEFAULT gen_random_uuid(),
    supplier_subscription_id UUID                     NOT NULL,
    data_id                  varchar(80)              NOT NULL,
    event_time               timestamp with time zone NULL,
    created_at               timestamp with time zone NOT NULL DEFAULT now(),

    CONSTRAINT fk_supplier_subscription
        FOREIGN KEY (supplier_subscription_id)
            REFERENCES supplier_subscription (id)
);
--endregion

--region Rename event data to acquirer event
ALTER TABLE event_data
    RENAME TO acquirer_event;

ALTER TABLE acquirer_event
    RENAME CONSTRAINT event_data_pkey TO acquirer_event_pkey;

ALTER INDEX ed_acquirer_subscription_id_index RENAME TO ae_acquirer_subscription_id_index;
ALTER INDEX ed_deleted_at_index RENAME TO ae_deleted_at_index;
ALTER INDEX ed_polling_index RENAME TO ae_polling_index;

-- A not-null constraint will be added once data is reconciled
ALTER TABLE acquirer_event
    ADD supplier_event_id UUID NULL,
    ADD CONSTRAINT fk_supplier_event FOREIGN KEY (supplier_event_id) REFERENCES supplier_event (id) NOT VALID;

ALTER TABLE acquirer_event
    RENAME when_created TO created_at;

-- Create a view to facilitate zero downtime deployment. This should be removed in a future release
CREATE VIEW event_data AS
SELECT *, created_at as when_created
FROM acquirer_event;

ALTER TABLE event_api_audit
    RENAME TO acquirer_event_audit;

ALTER TABLE acquirer_event_audit
    RENAME CONSTRAINT event_api_audit_pkey TO acquirer_event_audit_pkey;

ALTER TABLE acquirer_event_audit
    RENAME when_created TO created_at;

-- Create a view to facilitate zero downtime deployment. This should be removed in a future release
CREATE VIEW event_api_audit AS
SELECT *, created_at as when_created
FROM acquirer_event_audit;
--endregion
