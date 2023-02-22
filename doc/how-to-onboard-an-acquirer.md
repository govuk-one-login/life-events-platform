# How to onboard an acquirer

## Contents

1. [Set up User Pool Client in Cognito](#set-up-user-pool-client-in-cognito)
2. [Set up the Acquirer through the API (if this is a new acquirer)](#set-up-the-acquirer-through-the-api-for-new-acquirers)
3. [Set up the Acquirer Subscription through the API](#set-up-the-acquirer-subscription-through-the-api)
4. [Tell the Acquirer what to do](#tell-the-acquirer-what-to-do)

## Set up User Pool Client in Cognito

Currently, we use terraform to do this, but in the future we can make it an api call in our app.

In `terraform/modules/cognito/clients.tf` add another `simple_user_pool_client` with the correct name for the client.
Copy the format of the other modules in that file, with the `scope` being set as
`"${local.identifier}/${local.scope_consume}"` for an acquirer.

## Set up the Acquirer through the API for new acquirers

In the GDX API, POST to `/acquirers` the name of this new acquirer, noting the `acquirerId`.

e.g.
POST `/acquirers`

```json
{
  "name": "ACQUIRER NAME"
}
```

Returns 200

```json
{
  "acquirerId": "ACQUIRER-ID-GUID",
  "name": "ACQUIRER NAME",
  "id": "ACQUIRER-ID-GUID"
}
```

## Set up the Acquirer Subscription through the API

Once the terraform changes have been made, go into the AWS console and find the newly created client at
`https://eu-west-2.console.aws.amazon.com/cognito/v2/home?region=eu-west-2` ->
`[USER POOL NAME (e.g. dev-gdx-data-share)]` -> `App integration` -> `App client list` -> `[CLIENT NAME]`.

Note down the `Client ID` and `Client secret`.

In the GDX API, POST to `/acquirers/{acquirerId}/subscriptions` (replacing `acquirerId` with the Acquirer ID) the data
of the new subscription:

```json
{
  "ingressEventType": "DEATH_NOTIFICATION",
  "oauthClientId": "Client ID",
  "enrichmentFields": "Fields to enrich"
}
```

e.g.
POST `/acquirers/ACQUIRER-ID-GUID/subscriptions`

```json
{
  "ingressEventType": "DEATH_NOTIFICATION",
  "oauthClientId": "an-oauth-client",
  "enrichmentFields": [
    "firstNames",
    "lastName",
    "dateOfDeath",
    "sex",
    "registrationDate"
  ]
}
```

Returns 200

```json
{
  "acquirerSubscriptionId": "ACQUIRER-SUBSCRIPTION-ID-GUID",
  "acquirerId": "ACQUIRER-ID-GUID",
  "oauthClientId": "an-oauth-client",
  "ingressEventType": "DEATH_NOTIFICATION",
  "enrichmentFields": [
    "firstNames",
    "lastName",
    "dateOfDeath"
  ],
  "id": "ACQUIRER-SUBSCRIPTION-ID-GUID"
}
```

## Tell the acquirer what to do

Read over [How to acquire data from the service](acquiring-data-from-the-service.md) and make sure it is up-to-date,
then send that over to the client so that they have the information. They need the client ID, client secret, and the name
of the environment.
