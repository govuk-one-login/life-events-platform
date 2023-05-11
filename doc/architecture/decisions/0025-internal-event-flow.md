# 25. Internal event flows

[Next >>](9999-end.md)

Date: 2023-05-10

## Status

Accepted

## Context
As described in [ADR 24](0024-flat-file-ingestion-poc.md), we are likely to need to store sensitive data for some amount
of time to allow us to deliver death notification. We need to know when we no longer need the information, so that we can
delete it, consistent with our principals of minimising data retention.

Currently, it is not possible to establish if an event has been fully consumed or if further acquirer events will be created
just from looking at the database.

## Decision
We have decided to slightly modify the existing fan-out of events to ensure all acquirer events are persisted to the database
at the same time as the supplier event. This means that it is always possible to determine if the source event data should
be deleted just by looking at the state of the acquirer events in the database. Once all acquirer events have been consumed,
or an agreed length of time has passed, the data can be deleted.

The enrichment and delivery of events is the most likely step to fail, and this is still isolated to an individual acquirer
event level.


### Implementation
The Supplier Event Processor will be updated to persist the supplier event and all created acquirer event in a single transaction.
The Acquirer Event Processor will be updated to use the previously created acquirer event records. It will ignore messages
that do not have a corresponding database entry. This will prevent duplicate events from being created.


[Next >>](9999-end.md)
