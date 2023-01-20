CREATE SEQUENCE death_registration_ids;

UPDATE death_registration_v1
SET id=nextval('death_registration_ids');
UPDATE death_registration_v1
SET data = jsonb_set(data::jsonb, '{id}', to_jsonb(id));
ALTER TABLE death_registration_v1
    ALTER COLUMN id SET DEFAULT nextval('death_registration_ids');
