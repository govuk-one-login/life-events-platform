# 8. Use database for audit logs

[Next >>](0009-use-queue-to-buffer-input-events.md)

Date: 2023-01-20

## Status

Draft

## Context

In the initial pass at the application structure, when potential scale was unknown, SQS queues were set up for audit events.
Given what we now know, auditing in this way is relatively complex infrastructurally, and we need a persistent store of audit events anyway.
The options would be to extend the queue with a consumer persisting the data to a database or S3, or to directly store audit events in a database.

In audit events where we transmit a payload of e.g. enriched data that may contain PII/sensitive data, we should store a hash of the payload.

## Decision

Use database directly for audit events.

## Consequences

- We need to be careful to not persist any sensitive/otherwise problematic data in the audit store
- We need to remove the audit queue and related entities
- We need to adjust the auditing of events, including
  - data being received from a provider (e.g. HMPO)
  - data being read by a consumer (e.g. DWP)
  - data being marked as consumed by a consumer (e.g. DWP)

[Next >>](0009-use-queue-to-buffer-input-events.md)
