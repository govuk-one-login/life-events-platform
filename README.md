# GDX Data Share POC

**Data Sharing POC Microservice**

## API Contracts
API documentation can be found  [here](doc/apicontracts/api-contract.md)

## Running locally

For running locally against docker instances of the following services:

- run this application independently e.g. in IntelliJ

`docker-compose up --scale gdx-data-share-poc=0`

## Running all services including this service

`docker-compose up`

## Running locally against test services

This is straight-forward as authentication is delegated down to the calling services.  Environment variables to be set are as follows:-
```
API_BASE_URL_OAUTH=https://sign-in-dev.hmpps.service.justice.gov.uk/auth
```

## Running integration tests

Before running integration tests you need to start a localstack instance

`docker-compose up localstack`

## Publishing a received message to your local instance

This assumes you have the [AWS CLI](https://aws.amazon.com/cli/) installed

1. Follow [Running Locally](#running-locally) to bring up the service and docker containers
2. Find the ARN of the Domain Events topic created in your localstack instance and update the `topic-arn` parameter in the command below
    ```shell
    aws --endpoint-url=http://localhost:4566 sns publish \
    --topic-arn arn:aws:sns:eu-west-2:000000000000:[find this in the log] \
    --message-attributes '{
      "eventType": { "DataType": "String", "StringValue": "death" }
    }' \
    --message '{
      "eventType":"death",
      "id": 123456789
    }'

    ```
3. Paste the command into your terminal

**NOTE**: If you get a `Topic does not exist` error, it may mean your default AWS profile points to a different region,
be sure it points to `eu-west-2` either by changing your default profile or by passing `--region eu-west-1` to the
command above.

### Receiving the published message from the datashare topic
1. Follow [Running Locally](#running-locally) to bring up the service and docker containers
2. Find the ARN of the queue (this is currently `dwp-event-queue`:
   ```shell
   aws --endpoint-url=http://localhost:4566 sqs \
   receive-message --queue-url http://localhost:4566/000000000000/dwp-event-queue
   ```
3. Look in the response for the `id` attribute of the `Message` i.e.
```json
{
   "Messages": [
      {
         "MessageId": "b2035ea6-f837-d674-6366-e1465163ba17",
         "ReceiptHandle": "pptjbkanqamcieladicbhlgndiftjpjwyqdfmfceadncftltdhgzcruxoiwmmvabghnfpvuxgturwkiyyfvaxxrkfhwjfuvhuujrkhuslyjxwgeztjkcujwcwnyxoxqpsxrfcjmdtemaccvbpdrtjjdnilbeafxkbxtunortuvyqjvxjuwdoubrkh",
         "MD5OfBody": "f5bb203e64125655ada249d47bc6290d",
         "Body": "{\"Type\": \"Notification\", \"MessageId\": \"eb226220-f31d-4945-88e7-adc3febb8d22\", \"TopicArn\": \"arn:aws:sns:eu-west-2:000000000000:5e2615e8-2916-4161-8e0c-f92331788107\", \"Message\": \"{\\\"eventType\\\":\\\"citizen-death\\\",\\\"id\\\":\\\"9e51f4e8-c032-4fd7-a1e2-0d7578ddf29a\\\",\\\"version\\\":\\\"1.0\\\",\\\"occurredAt\\\":\\\"2022-10-12T17:57:13.005574+01:00\\\",\\\"description\\\":\\\"Citizen Event: citizen-death\\\"}\", \"Timestamp\": \"2022-10-12T16:57:13.023Z\", \"SignatureVersion\": \"1\", \"Signature\": \"EXAMPLEpH+..\", \"SigningCertURL\": \"https://sns.us-east-1.amazonaws.com/SimpleNotificationService-0000000000000000000000.pem\", \"MessageAttributes\": {\"eventType\": {\"Type\": \"String\", \"Value\": \"citizen-death\"}}}"
      }
   ]
}
```
   Here the ID is `9e51f4e8-c032-4fd7-a1e2-0d7578ddf29a`

### Calling the API
1. Get and oauth bearer token from AUTH0 service 
2. Used this and the `application` to make the GET call


### Runbook


### Architecture

Architecture decision records start [here](doc/architecture/decisions/0001-use-adr.md)
