# 22. Allow the application to manage acquirer queues

[Next >>](9999-end.md)

Date: 2023-04-05

## Status

Accepted

## Context
We would like the application to be managed without code changes. That means that we need to onboard new acquirers without,
for example, writing terraform.

## Decision
To enable this, the application will be able to create and delegate access to SQS queues at runtime. The application will
have limited over the queue and full control over the KMS key associated with it.

## Consequences
This increases the risk slightly, but does not significantly increase the surface of the application. The application is
not granted permission to receive messages from the queue, not is it permitted to delete the queue. This minimises the
onwards impact of any application bugs or unauthorised access to the application.

[Next >>](9999-end.md)
