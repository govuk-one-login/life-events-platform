# 14. Retention Policy

[Next >>](0015-grafana.md)

Date: 2023-02-03

## Status

Draft

## Context

This system has the potential to transfer large quantities of sensitive citizen information. It is essential that we carefully
consider our data retention policy, and enforce it rigorously.

## Decision

Our current policy is to avoid persisting identifiable citizen information to the database, except where essential for
the functioning of the service. We store only supplier event identifiers - which may uniquely identify a citizen - to
fetch sensitive data just in time for our acquirers.
This reduces the risk of data being stolen while at rest, and also ensures data freshness for our acquirers.

As described in the [audit policy](0013-event-auditing.md), we store a cryptographic hash of citizen data we send to acquirers.
This is a one way transformation, such that the original data cannot be recovered - we can only check if the data sent is
the same as a sample.

## Assumptions
 - All citizen data payloads have sufficiently high entropy to make hashing effective in rendering the original data unrecoverable


## Future decisions
We need to consider a retention policy for personally identifiable supplier event identifiers, such as prisoner number.
This needs to take into account the requirements of consumers of the service to establish the time period over which this
information is essential to the delivery of this service.

## Future risks
Just-in-time data fetching may place a large amount of load on our suppliers. As we add more acquirers and higher throughput
event types, this may not be sustainable. We may need to revisit this policy to look at ways of caching data. Our intention
is that sensitive citizen data is always kept separate from event metadata and other longterm storage.



[Next >>](0015-grafana.md)

