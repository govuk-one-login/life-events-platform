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


insert into consumer_subscription (poll_client_id, callback_client_id, consumer_id, event_type_id, nino_required)
values ('dwp-event-receiver', 'dwp-event-receiver', (select id from event_consumer WHERE consumer_name = 'DWP Poller'),
        'DEATH_NOTIFICATION', true);
insert into consumer_subscription (poll_client_id, callback_client_id, consumer_id, event_type_id)
values ('dwp-event-receiver', 'dwp-event-receiver', (select id from event_consumer WHERE consumer_name = 'DWP Poller'),
        'LIFE_EVENT');

insert into consumer_subscription (callback_client_id, consumer_id, event_type_id, nino_required)
values ('hmrc-client', (select id from event_consumer WHERE consumer_name = 'Pub/Sub Consumer'),
        'DEATH_NOTIFICATION', true);
insert into consumer_subscription (callback_client_id, consumer_id, event_type_id)
values ('hmrc-client', (select id from event_consumer WHERE consumer_name = 'Pub/Sub Consumer'),
        'LIFE_EVENT');

insert into consumer_subscription (callback_client_id, consumer_id, event_type_id)
values ('internal-outbound', (select id from event_consumer WHERE consumer_name = 'Internal Adaptor'),
        'LIFE_EVENT');
insert into consumer_subscription (callback_client_id, consumer_id, event_type_id, nino_required)
values ('internal-outbound', (select id from event_consumer WHERE consumer_name = 'Internal Adaptor'),
        'DEATH_NOTIFICATION', true);

insert into consumer_subscription (consumer_id, event_type_id, push_uri, nino_required)
values ((select id from event_consumer WHERE consumer_name = 'S3 Consumer'),
        'DEATH_NOTIFICATION',
        's3://user:password@localhost', true);

insert into consumer_subscription (consumer_id, event_type_id, push_uri)
values ((select id from event_consumer WHERE consumer_name = 'S3 Consumer'),
        'LIFE_EVENT',
        's3://user:password@localhost');

insert into consumer_subscription (consumer_id, event_type_id, push_uri)
values ((select id from event_consumer WHERE consumer_name = 'Webhook Consumer'),
        'LIFE_EVENT',
        'http://localhost:8181/callback');
