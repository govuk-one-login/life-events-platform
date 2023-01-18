ALTER TABLE consumer_subscription
    ADD COLUMN enrichment_fields_included_in_poll boolean NOT NULL default false;

UPDATE consumer_subscription set enrichment_fields_included_in_poll = true;

UPDATE consumer_subscription set enrichment_fields_included_in_poll = false
WHERE consumer_id IN (SELECT id FROM consumer where name like 'DWP%');
