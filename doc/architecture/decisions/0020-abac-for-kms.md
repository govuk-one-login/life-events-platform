# 20. ABAC for KMS

[Next >>](9999-end.md)

Date: 2023-03-17

## Status

Draft

## Context

We would like the application to manage SQS queues for acquirers at runtime. This will allow building a management UI so
that changes can be made without a developer. For SQS, this is fine as we can delegate to the application the management
of queues with a name starting with a specified prefix. This cannot be done in the same way for KMS keys.

## Approach

Use tags to control application access to KMS keys.
 - Allow the application to create new keys
 - Allow the application to apply a specific tag to untagged keys
 - Allow the application to use keys tagged with a specific tag
 - Set up an AWS Config rule to ensure all keys are tagged

## Consequences

This adds some complexity to the access control of keys. We aim to mitigate this though enforcement with AWS Config and
documentation

[Next >>](9999-end.md)

