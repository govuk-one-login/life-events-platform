# 7. Use stepfunctions for example consumer

[Next >>](0008-use-database-for-audit.md)

Date: 2023-01-17

## Status

Accepted

## Context

To demonstrate ingesting and egressing events at scale, we require a scalable and flexible consumer of events.
Much of the structure is still subject to change, so it should be easy to change. Much of the design of this consumer is
not the same as a "real" consumer, as it artificially increases load and calls other APIs to verify outputs.

## Decision

We will use an AWS Stepfunction to orchestrate Lambda functions to consume the events.

## Consequences

- Increase in upfront complexity over e.g. just a lambda
- Increased flexibility: ease of scaling, adding extra layers

[Next >>](0008-use-database-for-audit.md)
