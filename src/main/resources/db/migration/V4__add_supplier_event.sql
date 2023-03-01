CREATE TABLE supplier_event
(
    id                       UUID PRIMARY KEY         NOT NULL DEFAULT gen_random_uuid(),
    supplier_subscription_id UUID                     NOT NULL,
    data_id                  varchar(80)              NOT NULL,
    event_time               timestamp with time zone NULL,
    created_at               timestamp with time zone NOT NULL DEFAULT now(),

    CONSTRAINT fk_supplier_subscription
        FOREIGN KEY (supplier_subscription_id)
            REFERENCES supplier_subscription (id)
)
