# 16. Push events directly to SQS
[Next >>](0017-mutual-tls.md)

Date: 2023-02-23

## Status

Proposal for discussion

## Context

Some technically mature acquirers require a queue type system for events, rather than the polling API interface currently
exposed. There are considerations around what the API boundary of the service is, and how we present events on a queue
while still meeting audit logging and data retention requirements. Our guiding principles here are:
- Protect sensitive data from unauthorised access
- Minimise the number of components exposed to sensitive information
- Maintain robust audit logging
- Minimise overall complexity

## Options

- Build an adapter to consume the API and push events to the queue
- Push events directly to the queue

## Decision
The kotlin service will push enriched ("thick") events directly to the acquirer queue. While this does increase the
complexity of the kotlin service, it minimises the overall system complexity. More importantly, it reduces the number of
components exposed to sensitive citizen data.

More broadly, the API surface of the system will be expanded to include the acquirer queue as well as the HTTP REST API.
AWS services such as SQS and S3 may be considered for inclusion in the API surface exposed, whereas third party systems
are more likely to need an adaptor. Authentication to AWS services can happen with ephemeral credentials created using IAM
roles, but external services will need longer term credentials. It is also easier to monitor compliance with policies using
built in AWS tooling. We should therefore consider anything external to AWS to outside the "core" service and so
requiring extra considerations.

## Impact
Exposing acquirer queues to the kotlin service will mean that queue clients need to be generated dynamically from data in
the database. This will require a rethink of the way instantiate clients, as we currently create beans for each client.
It also means that we can create the queues themselves from within the kotlin service, allowing us to keep a single API
endpoint to set up a new acquirer. Longer term, this means that acquirer management can happen from a user interface,
without requiring e.g. terraform changes to onboard or offboard an acquirer.

Defining the core service around AWS increases vendor lock-in.

[Next >>](0017-mutual-tls.md)

