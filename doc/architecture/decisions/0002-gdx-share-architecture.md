# 2. Service consume LEN events, and forward on to interested parties

[Next >>](0003-gdx-death-notification-event.md)


Date: 2022-09-10

## Status

Accepted

## Context
New service to aggregate and capture LEN related data in a single micro-service.


## Architecture
![This is the POC architecture{arch}](data_share_poc.svg)

Acceptance criteria.
- GDX will not store data unless it is to facilitate the service functionality.
- If data is stored it will not contain any personal information.
- Clients accessing the service will be subject to normal data sharing agreements
- Clients will be restricted to the data set agreed as part of that data sharing agreement
- Information on clients' access and the data sets they have received will be recorded.
- All data will be encrypted at rest and in transit

## Decision

This approach follows the agreed pattern of architecture for GDX

## Consequences

- Complexity will grow

[Next >>](0003-gdx-death-notification-event.md)
