# 3. Service will consume a death event from DWP, store and publish to interested clients

[Next >>](0004-baseline-aws-infrastructure.md)

Date: 2022-11-15

## Status

Accepted - Partially superceded [Use queues to buffer events](0009-use-queue-to-buffer-input-events.md)

## Context

New service to listen for death events from DWP and publish

## Architecture

![This is the Death event architecture{arch}](death-event-notifier.svg)

## Main Components

### Data Receiver API

- API for sending event information to GDS System. A standard will be defined for the communication protocol and format.

### Legacy Adaptor (inbound)

- Monitors and polls FTP server for incoming files and sends the data on to the Data Receiver API (now removed)

### Data processor

Data will be ingested from the Data Receiver API via an internal queue mechanism. (SQS)
Data may be transformed before temporary storage in a data cache. AWS DynamoDB is the preferred option,
Once stored a notification via a queue will be sent to the Event Publisher containing the UUID to the data set.

### Event Publisher

- Upon a received message an entry is recorded Event DB containing a unique Event UUID and a reference to the UUID
  required to access the dataset for the event.
- The event UUID is published on a SNS Topic, the message does not contain the data only a reference

### Event Retrieval API

- Clients who receive notifications of events will call back to this API to obtain the data payloads.
- This service will access the Event DB to find the data item in question and then retrieve it and return it to the
  client.

### Legacy Adaptor (Outbound)

- Now removed
- Services the needs of consuming clients that have a simpler technology stack.
- Will subscribe to the SNS event topic (like a high tech Dept) and then use that to assemble data for clients and send
  via FTP and Email.

## Decision

This approach follows the agreed pattern of architecture for GDX

[Next >>](0004-baseline-aws-infrastructure.md)
