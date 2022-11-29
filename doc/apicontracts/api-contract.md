## Documentation for API Endpoints

All URIs are relative to *http://localhost:8080*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*EventListenerApi* | [**publishEvent**](docs/EventListenerApi.md#publishEvent) | **POST** /event-data-receiver | Send events to GDS - The &#x27;Source&#x27; of the event - this could be HMPO or DWP for example
*EventPlatformApi* | [**getEvents**](docs/EventPlatformApi.md#getEvents) | **GET** /events | Returns all events for this client since the last call
*EventRehydrateApi* | [**getEventDetails**](docs/EventRehydrateApi.md#getEventDetails) | **GET** /event-data-retrieval/{id} | Event Rehydrate API - Lookup event data

## Documentation for Models

- [EventInformation](docs/EventInformation.md)
- [EventToPublish](docs/EventToPublish.md)
- [SubscribedEvent](docs/SubscribedEvent.md)

## Documentation for Authorization


### bearer-jwt