CREATE SEQUENCE death_registration_ids;

UPDATE death_registration_v1
SET id=nextval('death_registration_ids');
UPDATE lev.public.death_registration_v1
SET data = jsonb_set(data::jsonb, '{id}', to_jsonb(id));
