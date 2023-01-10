ALTER TABLE egress_event_data
    ADD COLUMN event_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now();

ALTER TABLE ingress_event_data
    ADD COLUMN event_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now();

ALTER TABLE egress_event_data
    DROP COLUMN data_expiry_time;

ALTER TABLE ingress_event_data
    DROP COLUMN data_expiry_time;
