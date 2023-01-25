ALTER TABLE event_data
    DROP COLUMN data_payload;

ALTER TABLE event_dataset
    DROP COLUMN store_payload;

DELETE
FROM publisher_subscription
WHERE dataset_id = 'DEATH_CSV';

DELETE
FROM event_dataset
WHERE id = 'DEATH_CSV'
