# GDX Death Notification POC

## API Contracts
API documentation can be found [here](https://d33v84mi0vopmk.cloudfront.net/webjars/swagger-ui/index.html)

## Running the service
### Running locally

See [contributing](CONTRIBUTING.md) for more info on running the service locally for development.

For running locally against docker instances of the following services:

- run this application independently e.g. in IntelliJ

`docker-compose -f docker-compose-local.yml up`

### Running all services including this service

`docker-compose up`

### Running remotely
The service is deployed to AWS, accessible through

| environment | url                                                   |
|-------------|-------------------------------------------------------|
| dev         | https://d2w5ifs4ir0pe.cloudfront.net/swagger-ui.html  |
| demo        | https://d33v84mi0vopmk.cloudfront.net/swagger-ui.html |

## Using the application to simulate events

The API schema is shown at `http://localhost:8080/swagger-ui.html`

First you need tell the GDX data receiver about the event.  This is done by calling the `/events` endpoint (04. Events) with a POST payload:

```json
{
    "eventType": "DEATH_NOTIFICATION",
    "id": "123456789"
}
```

Before this you need to obtain a Oauth2 bearer token, there is an OAUTH2 server build into the docker compose environment
```shell
curl --location --request POST 'http://localhost:9090/issuer1/token' \
--header 'Authorization: Basic Y2xpZW50OnNlY3JldA==' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=client_credentials' \
--data-urlencode 'scope=len'
```

This will return a token:
```json
{
  "token_type" : "Bearer",
  "access_token" : "eyJraWQiOiJpc3N1ZXIxIiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJsZW4iLCJuYmYiOjE2Njk4MjY0MzcsInNjb3BlIjoiZGF0YV9yZWNlaXZlci9ub3RpZnkiLCJpc3MiOiJodHRwOi8vb2F1dGgyOjgwODAvaXNzdWVyMSIsImV4cCI6MTY2OTgyNjczNywiaWF0IjoxNjY5ODI2NDM3LCJqdGkiOiJkYjE4NWMwZC1jMzFkLTQyYzMtOWM0Ni04Y2Q1ZTk1YzlkMmEiLCJjbGllbnRfaWQiOiJsZW4ifQ.dTu86j5jgIFbUBVi6yqCGg1qg_7oBycZvVXrwLSsxDBf1WqovS5MQcY5o3XrfXNVveCqTphVLwSJ3KkDk9VOFzrwAakaHDbT8V6jbFs2uzTGMzz4sX82Ls5Bs9es1xwkkNpQoDtBwz3JP614v-0G2iCcFcNLTvhE-b3M_o3CyYKPR_RifLKiAQqALouKbZPIv_8RPBrNn5kW50xj4RkjNm-yUXTuZi1F9Fxs_BcrdvY-slxLOJiWTCaNOnai2P_hQUVDMXVMD0caPDdkgD6dLsdKPyJ2cmU6L5kgQOYUCzDP4N1Qt1c_sjgBeyFiaTmLnDPsP5uTXwRp2JNfsHEgIw",
  "expires_in" : 3598,
  "scope" : "len"
}
```

This access token has the required scope of `events/publish` to access the endpoint :
```json
{
  "sub": "len",
  "nbf": 1669826698,
  "scope": "events/publish",
  "iss": "http://oauth2:8080/issuer1",
  "exp": 1669826998,
  "iat": 1669826698,
  "jti": "08a391a2-2657-4979-8049-aecd426122ae",
  "client_id": "len"
}
```

The token can then be used to call the events endpoint
```shell
curl --location --request POST 'http://localhost:8080/events' \
--header "Authorization: Bearer $TOKEN" \
--header 'Content-Type: application/json' \
--data-raw '{
    "eventType": "DEATH_NOTIFICATION",
    "id": "123456789"
}'
```

If this client is allowed to provide data there will be a mapping in the database between client Id and what events are allowed

| client\_id | client\_name | event\_type | dataset\_type | store\_payload | when\_created |
| :--- | :--- | :--- | :--- | :--- | :--- |
| len | HMPO | DEATH\_NOTIFICATION | DEATH\_LEV | false | 2022-11-30 16:20:02.364930 +00:00 |
| internal-inbound | Internal Inbound Adaptor | DEATH\_NOTIFICATION | DEATH\_CSV | true | 2022-11-30 16:20:02.364930 +00:00 |


This places an event on the event topic can be either be read from a **queue** or **polled**

### Getting events
Get a token for a valid client, in this instance we'll use `dwp`

```shell
curl --location --request POST 'http://localhost:9090/issuer1/token' \
--header 'Authorization: Basic Y2xpZW50OnNlY3JldA==' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=client_credentials' \
--data-urlencode 'scope=dwp'
```

The returning token contains the correct scope of `events/consume`
```json
{
  "sub": "dwp-event-receiver",
  "nbf": 1669826596,
  "scope": "events/consume",
  "iss": "http://oauth2:8080/issuer1",
  "exp": 1669826896,
  "iat": 1669826596,
  "jti": "90a9a1fb-4c03-43d3-839a-bf0162940078",
  "client_id": "dwp-event-receiver"
}
```

We can now get a list of events:

```shell
curl --location --request GET 'http://localhost:8080/events' \
--header "Authorization: Bearer $TOKEN"
```

This returns a list of events that have occurred and have not been deleted.

```json
[
    {
        "eventId": "d8a6f3ba-e915-4e79-8479-f5f5830f4622",
        "eventType": "DEATH_NOTIFICATION",
        "sourceId": "999999901",
        "eventData": {
            "firstName": "Joan Narcissus Ouroboros",
            "lastName": "SMITH",
            "dateOfDeath": "2008-08-08",
            "address": "888 Death House, 8 Death lane, Deadington, Deadshire"
        }
    }
]
```

Alternatively you can retrieve the event from a connected pub/sub queue

```shell
aws --endpoint-url=http://localhost:4566 sqs \
  receive-message --queue-url http://localhost:4566/000000000000/ogd-event-queue
```

This returns the event id in the message payload:
```json
{
    "Messages": [
        {
            "MessageId": "efba8087-0241-e573-ff6f-8323e44d6d7d",
            "ReceiptHandle": "rutsynbkhesjqrdeifabjqywxxfuxhjdhhehvranouvwkryobnjmueowfcnesnnousseeuxyrvctgbxlisioxphgyliyukgrkklhojstaioabkthizocffrhgnroseinoifdutqknoeueahstxocbukbkkcmmxfodagcbyojtgczimttxabcqxfwf",
            "MD5OfBody": "7c5924613fc5580006147077c2a37a2b",
            "Body": "{\"Type\": \"Notification\", \"MessageId\": \"043c18ca-b6b8-47cd-b555-0469c06c9963\", \"TopicArn\": \"arn:aws:sns:eu-west-2:000000000000:728876a5-273b-4b48-9c75-acc6317fa304\", \"Message\": \"{\\\"eventType\\\":\\\"DEATH_NOTIFICATION\\\",\\\"id\\\":\\\"4c1f7599-f7b4-43c8-9c0e-cd59be7e717a\\\",\\\"version\\\":\\\"1.0\\\",\\\"occurredAt\\\":\\\"2022-11-30T16:41:31.908145919Z\\\",\\\"description\\\":\\\"Gov Event: DEATH_NOTIFICATION\\\"}\", \"Timestamp\": \"2022-11-30T16:41:32.135Z\", \"SignatureVersion\": \"1\", \"Signature\": \"EXAMPLEpH+..\", \"SigningCertURL\": \"https://sns.us-east-1.amazonaws.com/SimpleNotificationService-0000000000000000000000.pem\", \"MessageAttributes\": {\"eventType\": {\"Type\": \"String\", \"Value\": \"DEATH_NOTIFICATION\"}}}"
        }
    ]
}
```

### Getting a specific event
Using the retrieved event ID you can also get the full event data for a single event.  Depending on which client you use will return different data:
- The `dwp-event-receiver` client and the `hmrc-client` are examples that work here

#### Example for : `dwp-event-receiver`

Get the token:
```shell
curl --location --request POST 'http://localhost:9090/issuer1/token' \
--header 'Authorization: Basic Y2xpZW50OnNlY3JldA==' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=client_credentials' \
--data-urlencode 'scope=dwp'
```

Scope needed is `events/consume` which this token provides

then call with token and the event ID
```shell
curl --location --request GET 'http://localhost:8080/events/4c1f7599-f7b4-43c8-9c0e-cd59be7e717a' \
--header "Authorization: Bearer $TOKEN"
```

returns

```json
{
    "eventId": "4c1f7599-f7b4-43c8-9c0e-cd59be7e717a",
    "eventType": "DEATH_NOTIFICATION",
    "sourceId": "999999901",
    "eventData": {
        "firstName": "Joan Narcissus Ouroboros",
        "lastName": "SMITH",
        "dateOfDeath": "2008-08-08",
        "address": "888 Death House, 8 Death lane, Deadington, Deadshire"
    }
}
```

### Deleting events
Once you have received an event as a consumer, the event should be marked as consumed and removed from GDX, in our case by deleting the message.

Get a token for a valid client, in this instance we'll use `dwp` again

```shell
curl --location --request POST 'http://localhost:9090/issuer1/token' \
--header 'Authorization: Basic Y2xpZW50OnNlY3JldA==' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=client_credentials' \
--data-urlencode 'scope=dwp'
```

The returning token contains the correct scope of `events/consume`
```json
{
  "sub": "dwp-event-receiver",
  "nbf": 1669826596,
  "scope": "events/consume",
  "iss": "http://oauth2:8080/issuer1",
  "exp": 1669826896,
  "iat": 1669826596,
  "jti": "90a9a1fb-4c03-43d3-839a-bf0162940078",
  "client_id": "dwp-event-receiver"
}
```

We can now delete a specific event:

```shell
curl --location --request DELETE 'http://localhost:8080/events/4c1f7599-f7b4-43c8-9c0e-cd59be7e717a' \
--header "Authorization: Bearer $TOKEN"
```

This returns a 204 when the event is successfully deleted.

## Architecture

Architecture decision records start [here](doc/architecture/decisions/0001-use-adr.md)

## Glossary

There are numerous terms and acronyms used in this codebase that aren't immediately obvious, including

|Term| Definition                                                                                         |
|---|-----------------------------------------------------------------------------------------------------|
|GDS| Government Digital Service - https://www.gov.uk/government/organisations/government-digital-service |
|GDX| Government Data Exchange - This project and the wider programme of work                             |
|DWP| Department for Work and Pensions                                                                    |
|LEN| Life Event Notification (a service from HMPO)                                                       |
|HMPO| HM Passport Office                                                                                 |
|GRO| General Registry Office                                                                             |
|OGD| Other Government Department                                                                         |

