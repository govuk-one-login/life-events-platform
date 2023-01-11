# How to onboard a client

## On-boarding a Consumer

1. Set up User Pool Client in Cognito
2. Set up the Consumer through the API (if this is a new consumer)
3. Set up the Consumer Subscription through the API
4. Tell the Consumer what to do

### Set up User Pool Client in Cognito

Currently, we use terraform to do this, but in the future we can make it an api call in our app.

In `terraform/modules/cognito/clients.tf` add another `simple_user_pool_client` with the correct name for the client.
Copy the format of the other modules in that file, with the `scope` being set as
`"${local.identifier}/${local.scope_consume}"` for a consumer.

### Set up the Consumer through the API (if this is a new consumer)

In the GDX API, POST to `/consumers` the name of this new consumer, noting the `consumerId`.

e.g.
POST `/consumers`

```json
{
  "name": "CONSUMER NAME"
}
```

Returns 200

```json
{
  "consumerId": "CONSUMER-ID-GUID",
  "name": "CONSUMER NAME",
  "id": "CONSUMER-ID-GUID"
}
```

### Set up the Consumer Subscription through the API

Once the terraform changes have been made, go into the AWS console and find the newly created client at
`https://eu-west-2.console.aws.amazon.com/cognito/v2/home?region=eu-west-2` ->
`[USER POOL NAME (e.g. dev-gdx-data-share)]` -> `App integration` -> `App client list` -> `[CLIENT NAME]`.

Note down the `Client ID` and `Client secret`.

In the GDX API, POST to `/consumers/{consumerId}/subscriptions` (replacing `consumerId` with the Consumer ID) the data
of the new subscription:

```json
{
  "ingressEventType": "DEATH_NOTIFICATION",
  "oauthClientId": "Client ID",
  "enrichmentFields": "Fields to enrich"
}
```

e.g.
POST `/consumers/CONSUMER-ID-GUID/subscriptions`

```json
{
  "ingressEventType": "DEATH_NOTIFICATION",
  "oauthClientId": "an-oauth-client",
  "enrichmentFields": "firstName,lastName,dateOfDeath"
}
```

Returns 200

```json
{
  "consumerSubscriptionId": "CONSUMER-SUBSCRIPTION-ID-GUID",
  "consumerId": "CONSUMER-ID-GUID",
  "oauthClientId": "an-oauth-client",
  "ingressEventType": "DEATH_NOTIFICATION",
  "enrichmentFields": "firstName,lastName,dateOfDeath",
  "id": "CONSUMER-SUBSCRIPTION-ID-GUID"
}
```

### Tell the Consumer what to do

The Consumer will now need to actually use this to access our API. For this they will need to be able to get themselves
an access token, and then they will need to use that with the API.

In order to get the access token, they will need to call the Cognito User Pools domain in order to get their Bearer
authentication token. The base of the URL can be found in the `Cognito` -> `User Pool` -> `App integration` -> `Cognito
domain`. They then need to make a call like below:

e.g. POST to `BASEURL/oauth2/token`
```json
{
  "grant_type": "client_credentials",
  "client_id": "CLIENT ID",
  "client_secret": "CLIENT SECRET"
}
```
Returns 200
```json

{
  "access_token": "ACCESS-TOKEN",
  "expires_in": 3600,
  "token_type": "Bearer"
}
```

This access token is then used in the `Authentication` header of all requests to the API with value `Bearer ACCESS-TOKEN`
