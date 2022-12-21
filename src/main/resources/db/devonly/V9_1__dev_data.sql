insert into event_subscription (client_id, publisher_id, event_type_id, dataset_id)
select 'len', id, 'DEATH_NOTIFICATION', 'DEATH_LEV'
from event_publisher
WHERE publisher_name = 'HMPO';

insert into event_subscription (client_id, publisher_id, event_type_id, dataset_id)
values ('internal-inbound', (select id from event_publisher WHERE publisher_name = 'HMPO'),
        'LIFE_EVENT',
        'PASS_THROUGH');

insert into event_subscription (client_id, publisher_id, event_type_id, dataset_id)
values ('internal-inbound', (select id from event_publisher WHERE publisher_name = 'HMPO'),
        'DEATH_NOTIFICATION',
        'DEATH_CSV');

insert into egress_event_type (ingress_event_type, description, active)
values ('DEATH_NOTIFICATION', 'Death notification event for DWP', true),
       ('LIFE_EVENT', 'Life event for DWP', true),
       ('DEATH_NOTIFICATION', 'Death notification event for HMRC', true),
       ('LIFE_EVENT', 'Life event for HMRC', true),
       ('DEATH_NOTIFICATION', 'Death notification event for Internal Adaptor', true),
       ('LIFE_EVENT', 'Life event for Internal Adaptor', true),
       ('DEATH_NOTIFICATION', 'Death notification event for S3 Consumer', true),
       ('LIFE_EVENT', 'Life event for S3 Consumer', true),
       ('DEATH_NOTIFICATION', 'Death notification event for Webhook Consumer', true),
       ('LIFE_EVENT', 'Life event for Webhook Consumer', true);

insert into consumer_subscription (poll_client_id, callback_client_id, consumer_id, event_type_id, nino_required)
values ('dwp-event-receiver', 'dwp-event-receiver', (select id from event_consumer WHERE consumer_name = 'DWP Poller'),
        (select id from egress_event_type WHERE description = 'Death notification event for DWP'), true);
insert into consumer_subscription (poll_client_id, callback_client_id, consumer_id, event_type_id)
values ('dwp-event-receiver', 'dwp-event-receiver', (select id from event_consumer WHERE consumer_name = 'DWP Poller'),
        (select id from egress_event_type WHERE description = 'Life event for DWP'));

insert into consumer_subscription (callback_client_id, consumer_id, event_type_id, nino_required)
values ('hmrc-client', (select id from event_consumer WHERE consumer_name = 'Pub/Sub Consumer'),
        (select id from egress_event_type WHERE description = 'Death notification event for HMRC'), true);
insert into consumer_subscription (callback_client_id, consumer_id, event_type_id)
values ('hmrc-client', (select id from event_consumer WHERE consumer_name = 'Pub/Sub Consumer'),
        (select id from egress_event_type WHERE description = 'Life event for HMRC'));

insert into consumer_subscription (callback_client_id, consumer_id, event_type_id, nino_required)
values ('internal-outbound', (select id from event_consumer WHERE consumer_name = 'Internal Adaptor'),
        (select id from egress_event_type WHERE description = 'Death notification event for Internal Adaptor'), true);
insert into consumer_subscription (callback_client_id, consumer_id, event_type_id)
values ('internal-outbound', (select id from event_consumer WHERE consumer_name = 'Internal Adaptor'),
        (select id from egress_event_type WHERE description = 'Life event for Internal Adaptor'));

insert into consumer_subscription (consumer_id, event_type_id, push_uri, nino_required)
values ((select id from event_consumer WHERE consumer_name = 'S3 Consumer'),
        (select id from egress_event_type WHERE description = 'Death notification event for S3 Consumer'),
        's3://user:password@localhost', true);
insert into consumer_subscription (consumer_id, event_type_id, push_uri)
values ((select id from event_consumer WHERE consumer_name = 'S3 Consumer'),
        (select id from egress_event_type WHERE description = 'Life event for S3 Consumer'),
        's3://user:password@localhost');

insert into consumer_subscription (consumer_id, event_type_id, push_uri, nino_required)
values ((select id from event_consumer WHERE consumer_name = 'Webhook Consumer'),
        (select id from egress_event_type WHERE description = 'Death notification event for Webhook Consumer'),
        'http://localhost:8181/callback', true);
insert into consumer_subscription (consumer_id, event_type_id, push_uri)
values ((select id from event_consumer WHERE consumer_name = 'Webhook Consumer'),
        (select id from egress_event_type WHERE description = 'Life event for Webhook Consumer'),
        'http://localhost:8181/callback');
