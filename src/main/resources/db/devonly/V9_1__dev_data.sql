CREATE OR REPLACE FUNCTION getIdFromPublisherName(publisher_name_check varchar(80))
RETURNS UUID
    LANGUAGE plpgsql
    AS
$$  
Declare  
 publisher_id UUID;
Begin
  SELECT id
  INTO publisher_id
  FROM publisher WHERE name = publisher_name_check;
  RETURN publisher_id;
End;  
$$;

CREATE OR REPLACE FUNCTION getIdFromConsumerName(consumer_name_check varchar(80))
    RETURNS UUID
    LANGUAGE plpgsql
AS
$$
Declare
    consumer_id UUID;
Begin
    SELECT id
    INTO consumer_id
    FROM consumer WHERE name = consumer_name_check;
    RETURN consumer_id;
End;
$$;

INSERT INTO publisher_subscription
    (client_id, publisher_id, event_type_id, dataset_id)
VALUES
    ('len', getIdFromPublisherName('HMPO'), 'DEATH_NOTIFICATION', 'DEATH_LEV'),
    ('internal-inbound', getIdFromPublisherName('HMPO'), 'LIFE_EVENT','PASS_THROUGH'),
    ('internal-inbound', getIdFromPublisherName('HMPO'), 'DEATH_NOTIFICATION','DEATH_CSV');

INSERT INTO consumer_subscription
(poll_client_id, callback_client_id, consumer_id, nino_required, enrichment_fields, ingress_event_type)
VALUES
    ('dwp-event-receiver', 'dwp-event-receiver', getIdFromConsumerName('DWP Poller'), true, 'firstName,lastName,age', 'DEATH_NOTIFICATION'),
    ('dwp-event-receiver', 'dwp-event-receiver', getIdFromConsumerName('DWP Poller'), false, '', 'LIFE_EVENT');

INSERT INTO consumer_subscription
    (callback_client_id, consumer_id, nino_required, enrichment_fields, ingress_event_type)
VALUES
    ('hmrc-client', getIdFromConsumerName('Pub/Sub Consumer'), true, 'firstName,lastName,age', 'DEATH_NOTIFICATION'),
    ('hmrc-client', getIdFromConsumerName('Pub/Sub Consumer'), false, '', 'LIFE_EVENT');

INSERT INTO consumer_subscription
    (callback_client_id, consumer_id, nino_required, is_legacy, enrichment_fields, ingress_event_type)
VALUES
    ('internal-outbound', getIdFromConsumerName('Internal Adaptor'), true, true, 'firstName,lastName,age', 'DEATH_NOTIFICATION'),
    ('internal-outbound', getIdFromConsumerName('Internal Adaptor'), false, true, '', 'LIFE_EVENT');

INSERT INTO consumer_subscription
    (consumer_id, push_uri, nino_required, enrichment_fields, ingress_event_type)
VALUES
    (getIdFromConsumerName('S3 Consumer'), 's3://user:password@localhost', true, 'firstName,lastName,age', 'DEATH_NOTIFICATION'),
    (getIdFromConsumerName('S3 Consumer'), 's3://user:password@localhost', false, '', 'LIFE_EVENT'),
    (getIdFromConsumerName('Webhook Consumer'), 'http://localhost:8181/callback', true, 'firstName,lastName,age', 'DEATH_NOTIFICATION'),
    (getIdFromConsumerName('Webhook Consumer'), 'http://localhost:8181/callback', false, '', 'LIFE_EVENT');


DROP FUNCTION IF EXISTS getIdFromPublisherName;
DROP FUNCTION IF EXISTS getIdFromConsumerName;