ALTER TABLE egress_event_type
ADD COLUMN enrichment_fields TEXT NULL;

ALTER TABLE ingress_event_type
ADD COLUMN fields TEXT NULL;


UPDATE egress_event_type
SET enrichment_fields = 'firstName,lastName,age'
FROM egress_event_type eet
JOIN ingress_event_type iet on eet.ingress_event_type = iet.id
WHERE iet.id = 'DEATH_NOTIFICATION';

UPDATE egress_event_type
SET enrichment_fields = ''
FROM egress_event_type eet
JOIN ingress_event_type iet on eet.ingress_event_type = iet.id
WHERE iet.id != 'DEATH_NOTIFICATION';

UPDATE ingress_event_type
SET fields = 'firstName,lastName,age'
WHERE id = 'DEATH_NOTIFICATION';

UPDATE ingress_event_type
SET fields = ''
WHERE id != 'DEATH_NOTIFICATION';


ALTER TABLE egress_event_type
ALTER COLUMN enrichment_fields SET NOT NULL;

ALTER TABLE ingress_event_type
ALTER COLUMN fields SET NOT NULL;