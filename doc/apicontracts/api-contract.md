## Documentation for API Endpoints

All URIs are relative to *http://localhost:8080*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*EventListenerApi* | [**publishEvent**](docs/EventListenerApi.md#publishEvent) | **POST** /event-data-receiver | Send events to GDS - Source
*EventPlatformApi* | [**getEvents**](docs/EventPlatformApi.md#getEvents) | **GET** /events | Tell me about events
*EventRehydrateApi* | [**getEventDetails**](docs/EventRehydrateApi.md#getEventDetails) | **GET** /event-data-retrieval/{id} | Event Rehydrate API - Lookup event data

## Documentation for Models

- [EventInformation](docs/EventInformation.md)
- [EventToPublish](docs/EventToPublish.md)
- [SubscribedEvent](docs/SubscribedEvent.md)

## Documentation for Authorization


### bearer-jwt
