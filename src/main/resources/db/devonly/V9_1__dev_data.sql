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
  FROM event_publisher WHERE publisher_name = publisher_name_check;
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
    FROM event_consumer WHERE consumer_name = consumer_name_check;
    RETURN consumer_id;
End;
$$;

CREATE OR REPLACE FUNCTION getIdFromDescription(description_check varchar(200))
    RETURNS UUID
    LANGUAGE plpgsql
AS
$$
Declare
    type_id UUID;
Begin
    SELECT id
    INTO type_id
    FROM egress_event_type WHERE description = description_check;
    RETURN type_id;
End;
$$;

INSERT INTO event_subscription
    (client_id, publisher_id, event_type_id, dataset_id)
VALUES
    ('len', getIdFromPublisherName('HMPO'), 'DEATH_NOTIFICATION', 'DEATH_LEV'),
    ('internal-inbound', getIdFromPublisherName('HMPO'), 'LIFE_EVENT','PASS_THROUGH'),
    ('internal-inbound', getIdFromPublisherName('HMPO'), 'DEATH_NOTIFICATION','DEATH_CSV');

INSERT INTO egress_event_type
    (ingress_event_type, description, active, enrichment_fields)
VALUES
    ('DEATH_NOTIFICATION', 'Death notification event for DWP', true, 'firstName,lastName,age'),
    ('LIFE_EVENT', 'Life event for DWP', true, ''),
    ('DEATH_NOTIFICATION', 'Death notification event for HMRC', true, 'firstName,lastName,age'),
    ('LIFE_EVENT', 'Life event for HMRC', true, ''),
    ('DEATH_NOTIFICATION', 'Death notification event for Internal Adaptor', true, 'firstName,lastName,age'),
    ('LIFE_EVENT', 'Life event for Internal Adaptor', true, ''),
    ('DEATH_NOTIFICATION', 'Death notification event for S3 Consumer', true, 'firstName,lastName,age'),
    ('LIFE_EVENT', 'Life event for S3 Consumer', true, ''),
    ('DEATH_NOTIFICATION', 'Death notification event for Webhook Consumer', true, 'firstName,lastName,age'),
    ('LIFE_EVENT', 'Life event for Webhook Consumer', true, '');

INSERT INTO consumer_subscription
(poll_client_id, callback_client_id, consumer_id, event_type_id, nino_required)
VALUES
    ('dwp-event-receiver', 'dwp-event-receiver', getIdFromConsumerName('DWP Poller'), getIdFromDescription('Death notification event for DWP'), true),
    ('dwp-event-receiver', 'dwp-event-receiver', getIdFromConsumerName('DWP Poller'), getIdFromDescription('Life event for DWP'), false);

INSERT INTO consumer_subscription
    (callback_client_id, consumer_id, event_type_id, nino_required)
VALUES
    ('hmrc-client', getIdFromConsumerName('Pub/Sub Consumer'), getIdFromDescription('Death notification event for HMRC'), true),
    ('hmrc-client', getIdFromConsumerName('Pub/Sub Consumer'), getIdFromDescription('Life event for HMRC'), false),
    ('internal-outbound', getIdFromConsumerName('Internal Adaptor'), getIdFromDescription('Death notification event for Internal Adaptor'), true),
    ('internal-outbound', getIdFromConsumerName('Internal Adaptor'), getIdFromDescription('Life event for Internal Adaptor'), false);

INSERT INTO consumer_subscription
    (consumer_id, event_type_id, push_uri, nino_required)
VALUES
    (getIdFromConsumerName('S3 Consumer'), getIdFromDescription('Death notification event for S3 Consumer'), 's3://user:password@localhost', true),
    (getIdFromConsumerName('S3 Consumer'), getIdFromDescription('Life event for S3 Consumer'), 's3://user:password@localhost', false),
    (getIdFromConsumerName('Webhook Consumer'), getIdFromDescription('Death notification event for Webhook Consumer'), 'http://localhost:8181/callback', true),
    (getIdFromConsumerName('Webhook Consumer'), getIdFromDescription('Life event for Webhook Consumer'), 'http://localhost:8181/callback', false);


DROP FUNCTION IF EXISTS getIdFromPublisherName;
DROP FUNCTION IF EXISTS getIdFromConsumerName;
DROP FUNCTION IF EXISTS getIdFromDescription;