ALTER TABLE supplier_event
    ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE NULL;

UPDATE supplier_event
SET deleted_at = CURRENT_TIMESTAMP
WHERE id IN
      (
      SELECT se.id FROM supplier_event se
      JOIN acquirer_event ae ON ae.supplier_event_id = se.id
      GROUP BY se.id
      HAVING SUM( CASE WHEN ae.deleted_at IS NULL THEN 1 ELSE 0 END ) = 0
      );

CREATE INDEX ae_supplier_event_index
    ON acquirer_event (supplier_event_id);

CREATE INDEX se_deleted_at_index
    ON supplier_event (deleted_at);
