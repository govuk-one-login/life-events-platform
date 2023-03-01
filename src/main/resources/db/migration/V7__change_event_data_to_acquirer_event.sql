ALTER TABLE event_data
    RENAME TO acquirer_event;

-- A not-null constraint will be added in future
ALTER TABLE acquirer_event
    ADD supplier_event_id UUID NULL,
    ADD CONSTRAINT fk_supplier_event FOREIGN KEY (supplier_event_id) REFERENCES supplier_event (id) NOT VALID;

ALTER TABLE acquirer_event
    RENAME when_created TO created_at;

-- Create a view to facilitate zero downtime deployment. This should be removed in a future release
CREATE VIEW event_data AS
SELECT *, created_at as when_created
FROM acquirer_event;
