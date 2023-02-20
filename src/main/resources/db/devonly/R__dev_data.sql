CREATE OR REPLACE FUNCTION getIdFromSupplierName(supplier_name_check varchar(80))
    RETURNS UUID
    LANGUAGE plpgsql
AS
$$
Declare
    supplier_id UUID;
Begin
    SELECT id
    INTO supplier_id
    FROM supplier
    WHERE name = supplier_name_check;
    RETURN supplier_id;
End;
$$;

CREATE OR REPLACE FUNCTION getIdFromAcquirerName(acquirer_name_check varchar(80))
    RETURNS UUID
    LANGUAGE plpgsql
AS
$$
Declare
    acquirer_id UUID;
Begin
    SELECT id
    INTO acquirer_id
    FROM acquirer
    WHERE name = acquirer_name_check;
    RETURN acquirer_id;
End;
$$;

INSERT INTO supplier (name)
VALUES ('HMPO'),
       ('HMPPS');

INSERT INTO acquirer (name)
VALUES ('Prisoner Check Client'),
       ('DWP Poller'),
       ('Pub/Sub Consumer');

INSERT INTO supplier_subscription
    (client_id, supplier_id, event_type)
VALUES ('len', getIdFromSupplierName('HMPO'), 'DEATH_NOTIFICATION'),
       ('passthru', getIdFromSupplierName('HMPPS'), 'ENTERED_PRISON');

INSERT INTO acquirer_subscription
(oauth_client_id, acquirer_id, event_type, enrichment_fields_included_in_poll)
VALUES ('dwp-event-receiver', getIdFromAcquirerName('DWP Poller'), 'DEATH_NOTIFICATION', false),
       ('dwp-event-receiver', getIdFromAcquirerName('DWP Poller'), 'LIFE_EVENT', false),
       ('hmrc-client', getIdFromAcquirerName('Pub/Sub Consumer'), 'DEATH_NOTIFICATION', true),
       ('hmrc-client', getIdFromAcquirerName('Pub/Sub Consumer'), 'LIFE_EVENT', true),
       ('prisoner-check', getIdFromAcquirerName('Prisoner Check Client'), 'ENTERED_PRISON', false);

INSERT INTO acquirer_subscription_enrichment_field(acquirer_subscription_id, enrichment_field)
SELECT id,
       unnest(ARRAY ['SOURCE_ID', 'REGISTRATION_DATE', 'FIRST_NAMES', 'LAST_NAME', 'MAIDEN_NAME', 'DATE_OF_DEATH', 'DATE_OF_BIRTH', 'SEX', 'ADDRESS', 'BIRTH_PLACE', 'DEATH_PLACE', 'OCCUPATION', 'RETIRED'])
FROM acquirer_subscription
WHERE event_type = 'DEATH_NOTIFICATION'
  AND oauth_client_id IN ('dwp-event-receiver', 'hmrc-client');

INSERT INTO acquirer_subscription_enrichment_field(acquirer_subscription_id, enrichment_field)
SELECT id,
       unnest(ARRAY ['FIRST_NAME', 'LAST_NAME', 'MIDDLE_NAMES', 'DATE_OF_DEATH', 'GENDER', 'PRISONER_NUMBER'])
FROM acquirer_subscription
WHERE event_type = 'ENTERED_PRISON'
  AND oauth_client_id IN ('prisoner-check');

DROP FUNCTION IF EXISTS getIdFromSupplierName;
DROP FUNCTION IF EXISTS getIdFromAcquirerName;
