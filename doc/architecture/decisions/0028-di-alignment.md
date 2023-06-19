# 28. DI Alignment

[Next >>](9999-end.md)

Date: 2023-006-16

## Status

Proposed

## Context

Due to our move into the DI/One Login teams, we want to align our whole approach as much as possible with their current
patterns. This is because we want to make our system familiar and easy to access for support and other teams inside the
DI space, and to make sure our security and resiliency standings align with theirs.

## Scope

This document is looking at approaches to realign our system and architecture with these patterns. We are looking here
at how to replace the containerised central system with a more serverless approach, as is favoured in DI.

Deployment pipelines are out of scope for this ADR and will be considered separately.

## Approach

Our approach here is a large rework of our architecture, while maintaining a similar data flow and disrupting our
integrations as little as possible. Currently, the only major integration is DWP with access to a queue for receiving
events from our system, and with HMPO where we have already built a serverless ingestion flow.

### Event flow

The planned structure is detailed below, with some possible adapters for our acquirers listed afterwards, to show it can
integrate easily with other desired acquiring methods. This ordering is from supplying an event to acquiring it:

1. API - An API gateway that will take POST calls to add events at `/events/{eventType}`. This gateway will be
   authorised with a custom lambda authoriser, which will make sure the acquirer has the appropriate permissions to POST
   to that endpoint.
2. Validation - The API gateway endpoints will send their message after authorisation to validation lambdas that perform
   custom validation for that event type, we will have 1 per event type.
3. Enrichment - The validation lambdas post onto an SQS queue that allows us to have error handling, this queue is then
   consumed by an enrichment lambda that will enrich the event with all the enrichment of that event type.
4. SNS - The enrichment lambdas then put the event payload on SNS topics, 1 per event type, that will take the event
   payload from the enrichment lambdas.
5. Minimisation - SQS queues, 1 per acquirer per event type, will be subscribed to the SNS topic, and will then pass the
   information onto a minimisation lambda, which will restrict and minimise the event payload to only the enrichment
   fields that that specific acquirer has.
6. Delivery - The minimisation lambdas will then put the final minimised event payload onto the final acquirer queues,
   which our acquirers will then directly interface with.

![Image](di-alignment.svg)

### Acquirer and supplier management

In order to manage our acquirers and suppliers, we need to be able to spin up new sets of lambdas and queues.

For our suppliers, when we add one we will be adding a whole new event type, which means we will have to add both code
and infrastructure. We will need to spin up new versions of the validation, enrichment, and minimisation lambdas and
queues, as well as additions to our API gateway and authorisation lambda. As these are code changes as well as
infrastructure, this will be a slightly slower process than onboarding an acquirer, and as such can be done on the
frequency of new releases. This means that all the aforementioned will be managed through our code and in our IAAC, and
be deployed as part of a new release.

For our acquirers, we want to be able to add new acquirers or update acquirers to have more event types very easily and quickly. As a result, we want this to be possible through a simple API call, and not require any new releases. To achieve this, we will be using dynamic infrastructure to spin up new minimisation and delivery lambdas and queues. The minimsis

[Next >>](9999-end.md)
