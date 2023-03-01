ALTER TABLE event_api_audit
    RENAME TO acquirer_event_audit;

-- Create a view to facilitate zero downtime deployment. This should be removed in a future release
CREATE VIEW event_api_audit AS
SELECT *
FROM acquirer_event_audit;
