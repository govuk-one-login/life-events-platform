create index as_acquirer_id_index
    on acquirer_subscription (acquirer_id);

create index as_oauth_client_id_index
    on acquirer_subscription (oauth_client_id);

create index as_event_type_index
    on acquirer_subscription (event_type);

create index asef_acquirer_subscription_id_index
    on acquirer_subscription_enrichment_field (acquirer_subscription_id);

create index ed_acquirer_subscription_id_index
    on event_data (acquirer_subscription_id);

create index ed_deleted_at_index
    on event_data (deleted_at);

create index ed_polling_index
    on event_data (when_created, acquirer_subscription_id, deleted_at);

create index ss_supplier_id_index
    on supplier_subscription (supplier_id);

create unique index ss_client_search_ux
    on supplier_subscription (client_id, event_type);

create index ss_event_type_index
    on supplier_subscription (event_type);
