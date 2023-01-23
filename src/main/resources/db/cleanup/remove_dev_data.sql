DELETE FROM publisher_subscription WHERE client_id IN ('len','internal-inbound','internal-inbound');

DELETE FROM consumer_subscription WHERE oauth_client_id IN ('dwp-event-receiver','hmrc-client','internal-outbound');

DELETE FROM consumer_subscription WHERE push_uri IN ('s3://user:password@localhost','http://localhost:8181/callback');

DELETE FROM flyway_schema_history WHERE script = 'V9_1__dev_data.sql';
