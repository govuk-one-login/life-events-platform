# 6. Scopes and auth 

[Next >>](9999-end.md)


Date: 2023-01-03

## Status

Draft

## Context

There are several routes we have explored for the moment in regard to Authentication and Authorisation.
1. Have all auth exist in cognito and authorise with scopes
2. Have all auth exist locally
3. Have authentication exist in cognito, with very basic scopes, and then use our own database to narrow down subscription privileges.

Option 3 is what is being suggested for the moment, as this means we can use cognito to provide all the hard parts of auth, but use
our database for enrichment and subscription privileges as this is easier to work with at high speed.

## Current plan

### Scopes in cognito

1. events/publish - Allows the user to publish events to the system 
2. events/consume - Allows the user to consume events from the system
3. events/admin - Allows the user to use the admin and subscription features

### Fields in our DB

Each `Consumer` will have many `ConsumerSubscription`s, which will have an entry for `enrichmentFields` and an `eventType`.
These `enrichmentFields` will decide what extra data past the event ID that the consumer will receive upon calling for events.

Each `Publisher` will have many `PublisherSubscription`s, which will have an entry for `eventType` and a `datasetId`.
The `eventType` will show which event will be published, and the `datasetId` shows where enrichment data will come from.

### Limitations

1. We will have to have extra DB tables
2. We will have 2 sources of Auth
3. We will not be taking advantage of cognito for additional claims etc

### Benefits

1. We will be able to easily update enrichmentFields for a consumer
2. We will be able to easily modify the data structure if needed based on changing requirements
3. We will be able to still use cognito to lock down Authentication and base Authorisation

## Future possible plans

In the future, the current plan is to update this to remove `Consumer`s, `ConsumerSubscription`s, `Publisher`s, and 
`PublisherSubscription`s from the DB by using Cognito users with custom attributes (claims) to determine the event type,
enrichment fields, and event dataset. This will be easier when we have a more fixed data structure, and will remove the 
limitations of the current plan.


[Next >>](9999-end.md)
