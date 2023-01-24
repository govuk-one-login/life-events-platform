UPDATE event_type
SET fields = 'registrationDate,firstNames,lastName,maidenName,dateOfDeath,dateOfBirth,sex,address,birthplace,deathplace,occupation,retired'
WHERE id = 'DEATH_NOTIFICATION';

UPDATE consumer_subscription
SET enrichment_fields = et.fields
FROM event_type et
         JOIN consumer_subscription cs on et.id = cs.event_type
WHERE cs.event_type = 'DEATH_NOTIFICATION';
