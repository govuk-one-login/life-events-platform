ALTER TABLE supplier_event
    ADD COLUMN event_consumed BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE supplier_event
set event_consumed = true
WHERE event_consumed = false
  and EXISTS (SELECT 1
              FROM acquirer_event ae
              WHERE ae.supplier_event_id = supplier_event.id
              GROUP BY ae.supplier_event_id
              HAVING SUM(CASE when ae.deleted_at is null then 1 else 0 END) = 0)
