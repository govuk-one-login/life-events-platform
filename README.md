# GDX Data Share POC

**Data Sharing POC Microservice**

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
        --topic-arn arn:aws:sns:eu-west-2:000000000000:[find this in the log for HmppsTopicFactory] \
        --message-attributes '{
          "eventType": { "DataType": "String", "StringValue": "death" }
        }' \
        --message '{
          "eventType":"death",
          "id": 2323423
        }'
    ```
3. Paste the command into your terminal

**NOTE**: If you get a `Topic does not exist` error, it may mean your default AWS profile points to a different region,
be sure it points to `eu-west-2` either by changing your default profile or by passing `--region eu-west-1` to the
command above.

### Runbook


### Architecture

Architecture decision records start [here](doc/architecture/decisions/0001-use-adr.md)
