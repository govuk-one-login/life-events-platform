# 11. Event datasets and types

[Next >>](9999-end.md)

Date: 2023-01-26

## Status

Accepted

## Context

The system before this change had tables for both `event_type`s, and `event_dataset`s. This decision as we thought that
we would want to be able to both add new types of event (e.g. DEATH_NOTIFICATION) and new datasets (e.g. DEATH_LEV)
dynamically in the future, for new suppliers to the system.

However, 2 things mean that it makes less sense to have a dynamically updatable store for these.

1. If we were to add an event type or dataset to the flow, we would need to add code changes for mapping.
2. Event types are almost certainly going to only have one set of sources, so adding a dataset for each event type is
   just repeating information.

## Decision

We have decided to remove the `event_dataset` table, and any concept of datasets from the code.
The `event_type` table has been replaced with an enum of `EventType` instead.

## Consequences

- There is no longer a concept of event datasets, so event types can only be enriched from one set of sources and the
  system is simplified, allowing for faster iteration
- Any functionality involving event types can now be type safe through enums
- There are fewer database tables, simplifying the data structure

[Next >>](9999-end.md)
