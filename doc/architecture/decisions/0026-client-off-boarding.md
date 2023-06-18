# 26. Client off-boarding

[Next >>](0027-recovery-and-rollback-strategy.md)

Date: 2023-05-10

## Status

Proposed

## Context

When a client no longer wants to receive events from our system, we should have some way of off-boarding them. This
should include stopping sending them events and marking them as deleted, we do not want to fully delete them from our DB
until it is safe to do so with respect to data security.

## Approach

When we off-board a client, they should no longer be able to send or receive events. In order to do this, we mark the
subscription as deleted. Also, as we want to block their access to the system in its entirety, we will delete their SQS
queues if they have any, delete their Cognito Client, and mark all their events as consumed (if they are an acquirer).

We should have 4 new endpoints, only accessible by the admin:

1. Delete acquirer
2. Delete acquirer subscription
3. Delete supplier
4. Delete supplier subscription

Deleting the subscriptions is in case a supplier or acquirer would like to maintain another subscription, but remove
this one. In these cases, we will:

1. Mark the subscription as deleted, so that no new events get sent to/from it
2. For acquirer subscriptions, mark all non-consumed acquirer events as consumed
3. If there is a queue connected to the subscription, delete the queue
4. If there is a cognito client connected to the subscription, and the client is not connected to any non deleted
   subscriptions, delete the cognito client

Deleting the clients will be marking the client as deleted, and then performing the subscription deletions for all of
that clients subscriptions.

[Next >>](0027-recovery-and-rollback-strategy.md)
