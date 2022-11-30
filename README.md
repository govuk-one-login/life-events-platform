# GDX Death Notification POC

**Data Sharing POC Microservice**

## API Contracts
API documentation can be found  [here](doc/apicontracts/api-contract.md)

## Running locally

For running locally against docker instances of the following services:

- run this application independently e.g. in IntelliJ

`docker-compose -f docker-compose-local.yml up`

## Running all services including this service

`docker-compose up`

or from a published image:

`docker-compose -f docker-compose-full.yml up`

## Using the application to simulate events

First you need tell the GDX data receiver about the event.  This is done by calling the `event-data-receiver` with a POST payload:

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
  "expires_in" : 299,
  "scope" : "len"
}
```

This access token has the required scope of `data_receiver/notify` to access the endpoint :
```json
{
  "sub": "len",
  "nbf": 1669826698,
  "scope": "data_receiver/notify",
  "iss": "http://oauth2:8080/issuer1",
  "exp": 1669826998,
  "iat": 1669826698,
  "jti": "08a391a2-2657-4979-8049-aecd426122ae",
  "client_id": "len"
}
```

The token can then be used to call the event receiver
```shell
curl --location --request POST 'http://localhost:8080/event-data-receiver' \
--header 'Authorization: Bearer eyJraWQiOiJpc3N1ZXIxIiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJsZW4iLCJuYmYiOjE2Njk4MjY0ODksInNjb3BlIjoiZGF0YV9yZWNlaXZlci9ub3RpZnkiLCJpc3MiOiJodHRwOi8vb2F1dGgyOjgwODAvaXNzdWVyMSIsImV4cCI6MTY2OTgyNjc4OSwiaWF0IjoxNjY5ODI2NDg5LCJqdGkiOiJiNDdjMzIwNC0xMGMwLTQ0MmMtODVmNS1hMTM0ZWViYTljY2QiLCJjbGllbnRfaWQiOiJsZW4ifQ.Kf-IAxyQJe6Nk16CdYFyl-NPWCce6T6iOBS9-Ex2iiCGbnllnqYB2ELRt0U1VUuGQbmKBfecXUfbPXM553rgvYWwS_21ArdGImnvDCmzv8HyszUsOCfeAeXzne3sJI4SE5UiKAfnUUdGiDEFzhV2vpbNW4Oc8I-NkFI8okkec26fWfRuWvCaPTf-eYRK07CNh4xXHMiuqihhwxDrRV1Bh7bb8Zw2FN_ykkuoZUm5uDUqrY1WsclnX8S0ejdpOMWPRkh-XU2jAFC_2Ck3mtdf7ADDrQrlOFmdDgprDneNissTMTFpLzoWZ4HNh_gYR7cZWNxOJH1BQzVIw1_iUHZeHg' \
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

### To Poll event
Get a token for a valid client, in this instance we'll use `dwp`

```shell
curl --location --request POST 'http://localhost:9090/issuer1/token' \
--header 'Authorization: Basic Y2xpZW50OnNlY3JldA==' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=client_credentials' \
--data-urlencode 'scope=dwp'
```

The returning token contains the correct scope of `events/poll`
```json
{
  "sub": "dwp-event-receiver",
  "nbf": 1669826596,
  "scope": "data_retriever/read events/poll",
  "iss": "http://oauth2:8080/issuer1",
  "exp": 1669826896,
  "iat": 1669826596,
  "jti": "90a9a1fb-4c03-43d3-839a-bf0162940078",
  "client_id": "dwp-event-receiver"
}
```

We can now poll for events:

```shell
curl --location --request GET 'http://localhost:8080/events' \
--header 'Authorization: Bearer eyJraWQiOiJpc3N1ZXIxIiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJkd3AtZXZlbnQtcmVjZWl2ZXIiLCJuYmYiOjE2Njk4MjY4MDMsInNjb3BlIjoiZGF0YV9yZXRyaWV2ZXIvcmVhZCBldmVudHMvcG9sbCIsImlzcyI6Imh0dHA6Ly9vYXV0aDI6ODA4MC9pc3N1ZXIxIiwiZXhwIjoxNjY5ODI3MTAzLCJpYXQiOjE2Njk4MjY4MDMsImp0aSI6ImYxZmY1YjIyLTZiZDEtNDQ3My04MTQwLTQwNTVmOTk1YThiOSIsImNsaWVudF9pZCI6ImR3cC1ldmVudC1yZWNlaXZlciJ9.n4ct6P6lisuPNQRta3esrA6MB-2Yc5Wzi2iOqAe1t9-uklOMStcuyQrN87r6a7_1pmhR07gI0ETXjHKXFjDljoU6x5PtfzFKps9d9AubOOypz4_MR1kOUKpbBLC1zcE9XKCEs4KxE8nhdjyJ4hXbVKbBqh7jZsuln2O-xiaQrAGLLJ1vbczcAWpyDNttDZ1kTadnuUMz0a-tU5LYjiBWqQSBg-fLu3ilkF8kt8ZnbQnCBY2Vx9Gyqpwi7oSPWz73BxCU0jY_eA6FeuRTYlSTsDejopqn7WioiWlrRd1qOFPO-Wry6DweOJAiPAg56ssFnRh2v_LgWV3pFWQNDUJtmQ'
```

This returns a list of events that have occurred since the last poll.

```json
[
    {
        "eventId": "4c1f7599-f7b4-43c8-9c0e-cd59be7e717a",
        "eventType": "DEATH_NOTIFICATION",
        "eventTime": "2022-11-30T16:41:31.908146"
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

### Getting the data
Using the retrieved event ID you can now get the full event data.  Depending on which client you use will return different data:
- The `dwp-event-receiver` client also gets NINO data by calling a mocked HMRC API
- The `hmrc-client` client only gets the death core data

#### Examples for : `dwp-event-receiver`

Get the token:
```shell
curl --location --request POST 'http://localhost:9090/issuer1/token' \
--header 'Authorization: Basic Y2xpZW50OnNlY3JldA==' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=client_credentials' \
--data-urlencode 'scope=dwp'
```

Scope needed is `data_retriever/read` which this token provides

then call with token and the event ID
```shell
curl --location --request GET 'http://localhost:8080/event-data-retrieval/4c1f7599-f7b4-43c8-9c0e-cd59be7e717a' \
--header 'Authorization: Bearer eyJraWQiOiJpc3N1ZXIxIiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJkd3AtZXZlbnQtcmVjZWl2ZXIiLCJuYmYiOjE2Njk4MjcyNTcsInNjb3BlIjoiZGF0YV9yZXRyaWV2ZXIvcmVhZCBldmVudHMvcG9sbCIsImlzcyI6Imh0dHA6Ly9vYXV0aDI6ODA4MC9pc3N1ZXIxIiwiZXhwIjoxNjY5ODI3NTU3LCJpYXQiOjE2Njk4MjcyNTcsImp0aSI6IjQ1YTAxYTM2LTc2NDctNGJhMy04MzQ0LTBlNDEyMzY3OGUzYyIsImNsaWVudF9pZCI6ImR3cC1ldmVudC1yZWNlaXZlciJ9.EVLvi1QELzY29RsU8TLxOB12vBz0B-YKdWLMNjJiSu2lXjdxsqnhxBV64uSWzOOeRsUAqn6lHuE29o837njUW7LnyW1ag878RpTliISv2RNumxmjSJFpixeeSDZqNymp7K8mNkCk0S-K0eI2tE6q-1ACaV8CBqqcNSvsXWcQk2xvY0_xwwLUxCbExDo_EStOQ_ievuI71_kQt9rhtlSL_lQfJAfgAzBSRqxASTZRQeql-IWbwPWa8_GANSKTT2lKslSaL-leunm4BOjO0QnJ7_Fl_ysP6oVIcAX4kcOTLmyt-g3jFE3u7WpzS-AW6voF4yz7LvFhJD25YQ0S8bnRSw'
```

returns

```json
{
    "eventType": "DEATH_NOTIFICATION",
    "eventId": "4c1f7599-f7b4-43c8-9c0e-cd59be7e717a",
    "details": {
        "deathDetails": {
            "forenames": "Joan Narcissus Ouroboros",
            "surname": "SMITH",
            "dateOfBirth": "2008-08-08",
            "dateOfDeath": "2008-08-08",
            "sex": "Indeterminate",
            "address": "888 Death House, 8 Death lane, Deadington, Deadshire"
        },
        "additionalInformation": {
            "nino": "NS327601F"
        }
    }
}
```

#### Examples for : `hmrc-client`

Get the token:
```shell
curl --location --request POST 'http://localhost:9090/issuer1/token' \
--header 'Authorization: Basic Y2xpZW50OnNlY3JldA==' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=client_credentials' \
--data-urlencode 'scope=hmrc'
```

Scope needed is `data_retriever/read` which this `hmrc-client` token provides

then call with token and the event ID
```shell
curl --location --request GET 'http://localhost:8080/event-data-retrieval/4c1f7599-f7b4-43c8-9c0e-cd59be7e717a' \
--header 'Authorization: Bearer eyJraWQiOiJpc3N1ZXIxIiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJobXJjLWNsaWVudCIsIm5iZiI6MTY2OTgyNzU1Niwic2NvcGUiOiJkYXRhX3JldHJpZXZlci9yZWFkIGV2ZW50cy9wb2xsIiwiaXNzIjoiaHR0cDovL29hdXRoMjo4MDgwL2lzc3VlcjEiLCJleHAiOjE2Njk4Mjc4NTYsImlhdCI6MTY2OTgyNzU1NiwianRpIjoiNWE3MWJkODItOTg4Yi00YWEzLThiZTktOTQ5ZWVlYzk0NDYzIiwiY2xpZW50X2lkIjoiaG1yYy1jbGllbnQifQ.A6RAkjQImTlpkHiT0H423yALfSwJM2yHRwRaK7YPu1EXO_4WEcUsjHN4OMHFkItkN6ABc9oQhcFZTTUa2vXfFXktC4gI66kCpq8ttw52LxR1uTxC6YZxidSFZCxlUt_1gelIuTLwCCAOjiydT-EjvGeuYS6w6tRX1kbALKmWBJqfwpqQ3mR5Pzrgrpn9Yqqp2AeaH1YQa3Jl5zFexK8CezPF4CgGQzI-ctoeEtUfjn8ojn9TGLGGLVV7KpHCptRNgXSc_JCZM-ZymTVoBU4Y_M6RK_6zUZ6ja-PBBhm7RZewgl9mJDWVlV26QUpXA-Rzmz-NW6jd7fY_quvHkYla5w'
```

returns

```json
{
   "eventType": "DEATH_NOTIFICATION",
   "eventId": "4c1f7599-f7b4-43c8-9c0e-cd59be7e717a",
   "details": {
      "deathDetails": {
         "forenames": "Joan Narcissus Ouroboros",
         "surname": "SMITH",
         "dateOfBirth": "2008-08-08",
         "dateOfDeath": "2008-08-08",
         "sex": "Indeterminate",
         "address": "888 Death House, 8 Death lane, Deadington, Deadshire"
      }
   }
}
```

Notice how the NINO is not returned. This is because the client is not setup to need this:

| client\_id | client\_name | allowed\_event\_types | last\_poll\_event\_time | nino\_required |
| :--- | :--- | :--- | :--- | :--- |
| hmrc-client | HMRC | DEATH\_NOTIFICATION | null | false |
| internal-outbound | Internal Outbound Adaptor | DEATH\_NOTIFICATION | null | true |
| dwp-event-receiver | DWP | DEATH\_NOTIFICATION,BIRTH\_NOTIFICATION | 2022-11-30 16:41:31.908146 +00:00 | true |



### Architecture

Architecture decision records start [here](doc/architecture/decisions/0001-use-adr.md)
