# How to acquire data from the service

## Contents

1. [Required information](#required-information)
2. [How to authenticate](#how-to-authenticate)
3. [Calling the API](#calling-the-api)

## Required information

In order to acquire data from the service, a client will need:

1. A [client ID and secret](#client-id-and-secret)
2. The url to call for [Oauth](#oauth-base-url)
3. The [service's API url](#service-api-url)

### Client ID and Secret

This will be provided by the GDX team upon request.

### Oauth Base URL

| environment | url                                                          |
|-------------|--------------------------------------------------------------|
| dev         | https://dev-gdx-data-share.auth.eu-west-2.amazoncognito.com  |
| demo        | https://demo-gdx-data-share.auth.eu-west-2.amazoncognito.com |

### Service API URL

| environment | url                                    | swagger url                                           |
|-------------|----------------------------------------|-------------------------------------------------------|
| dev         | https://d33v84mi0vopmk.cloudfront.net  | https://d33v84mi0vopmk.cloudfront.net/swagger-ui.html |
| demo        | https://d2w5ifs4ir0pe.cloudfront.net   | https://d2w5ifs4ir0pe.cloudfront.net/swagger-ui.html  |

## How to authenticate

In order to call the API, a client needs to have an Authorization header, with a Bearer token. This token can be got
from the services oauth system using the client ID and secret already provided.

1. [Getting a token](#getting-a-token)
2. [Using the token](#using-the-token)

### Getting a token

See our public facing [TechDocs](https://alphagov.github.io/gdx-data-share-poc/how-to-generate-an-access-token.html).

### Using the token

Once the client has a token, this is then used as an authorization bearer token. For any requests to the API, simply add
the header `Authorization: Bearer [Token]` to your request, and you will be authorized and authenticated with this
token.

When using the swagger api, to use the token simply click the `Authorize` button at the top of the web page, fill the
token into the `bearer-jwt  (http, Bearer)` input, and click authorize.

## Calling the API

As an acquirer of events from the service, there are 3 important endpoints to know about:

1. `GET /events`
    1. This endpoint returns all the events that the client has, paginated and filtered by the query parameters
       specified on the Swagger endpoint.
    2. The events returned from this endpoint will either be not enriched, or if the client has specified these can also
       be enriched.
2. `GET /events/[event ID]`
    1. This endpoint will return the enriched event for the given event ID
3. `DELETE /events/[event ID]`
    1. This endpoint will mark the event for the given ID as deleted

The service expects a client to call `GET /events` to acquire their events, and then follow this up with calls
to `DELETE /events/[event id]` (and `GET /events/[event id]` if the first call does not return enriched events) for
every event they have consumed. This will result in the client not receiving those consumed events the next time they
call `GET /events`.

### FAQs

* How many events can be returned from `GET /events`?
    * This endpoint is paginated and the acquirer can specify any page size they want.
* What is the `[event ID]` for event specific queries?
    * This is the `id` property of every object in the array `data` in the response from `GET /events`.
* If there are no events, what will the response be from `GET /events`?
    * The array `data` will be empty, and `meta.page.totalElements` will be 0.

