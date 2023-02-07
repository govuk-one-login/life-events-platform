# 13. Event Auditing Process

[Next >>](0014-retention-policy.md)

Date: 2023-02-01

## Status

Accepted

## Context

The system as is passes a lot of data through to acquirers from suppliers. In order to record that the data has been
consumed correctly by the right acquirers, an audit trail for interacting with the data is needed.

This audit will need to include when calls from acquirers are made, to what endpoint, and a way of identifying the data
interacted with.

## Decision

For the moment, while we are only using 1 database, we are adding a table to the existing database called
event_api_audit.

This table has 6 columns:

1. id
2. oauth_client_id
    1. for tracking the acquirer that consumed the data
3. url
    1. this and the below let us know what functionality the acquirer is doing: getting all events, a specific event, or
       deleting an event
4. request_method
    1. this and the above let us know what functionality the acquirer is doing: getting all events, a specific event, or
       deleting an event
5. payload
    1. this is a json encoded payload of the information the user has interacted with, it contains a list of the data
    2. each entry will have:
        1. the ID of the event in our DB
        2. the ID of the event from the source
        3. the data contained of the event, hashed to remove all PII
6. when_created
    1. when the audit record is created, letting us know when the call happened

## Future options

In the future we expect the auditing to be in its own database so that we can lock down access much more, but for the
moment this is less important as we are only in development and prototyping.

[Next >>](0014-retention-policy.md)
