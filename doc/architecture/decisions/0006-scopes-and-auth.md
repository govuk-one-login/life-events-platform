# 6. Scopes and auth

[Next >>](0007-use-stepfunction-for-example-consumer.md)

Date: 2023-01-03

## Status

Accepted

## Context

At the moment we have authentication and authorisation with Cognito, using 5 scopes, as well as authorisation using our
internal database to narrow things down. These are relatively confused scopes, and the authentication and authorisation
of Cognito is currently relatively tricky to understand and manage, as a result there are some changes we would like to
make now and later in the future.

There are several routes we have explored in regard to Authentication and Authorisation.

1. Have all auth exist in Cognito and authorise with scopes
2. Have all auth exist locally
3. Have authentication exist in Cognito, with very basic scopes (trim the current number down to 3), and then use our
   own database to narrow down subscription privileges

Option 3 is what is being suggested for the immediate moment, as this means we can use Cognito to provide all the hard
parts of auth, but use our database for enrichment and subscription privileges as this is easier to work with at high
speed, as well as being only a small change and some tidying to our current solution.

## Current plan

### Scopes in Cognito

1. `events/publish` - Allows the user to publish events to the system
2. `events/consume` - Allows the user to consume events from the system
3. `events/admin` - Allows the user to use the admin and subscription features

### Fields in our DB

Each `Consumer` will have many `ConsumerSubscription`s, which will have an entry for `enrichmentFields` and
an `eventType`. These `enrichmentFields` will decide what extra data past the event ID that the consumer will receive
upon calling for events.

Each `Publisher` will have many `PublisherSubscription`s, which will have an entry for `eventType`.
The `eventType` will show which event will be published.

### Limitations

1. We will have to have extra DB tables
2. We are splitting some authorisation decisions between two places (Cognito and internal)
3. We will not be taking advantage of Cognito for additional claims etc

### Benefits

1. We will be able to easily update enrichmentFields for a consumer
2. We will be able to easily modify the data structure if needed based on changing requirements
3. We will be able to still use Cognito to lock down Authentication and base Authorisation

## Future possible plans

In the future, the current plan is to update this to remove `Consumer`s, `ConsumerSubscription`s, `Publisher`s, and
`PublisherSubscription`s from the DB by using Cognito users with custom attributes (claims) to determine the event type,
enrichment fields, and event dataset. This will be easier when we have a more fixed data structure, and will remove the
limitations of the current plan, though will have some limitations itself. Each user will have a limit of 50 custom
attributes assigned to them, each of which will be mapped such that the value of the attribute is the name of a field to
enrich. This means that one user can only consume up to 50 enriched fields. Also, if one consumer consumes multiple types
of event, they will have one user per event type consumed.

[Next >>](0007-use-stepfunction-for-example-consumer.md)
