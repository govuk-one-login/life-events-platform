# EventPlatformApi

All URIs are relative to *http://localhost:8080*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getEvents**](EventPlatformApi.md#getEvents) | **GET** /events | Returns all events for this client since the last call

<a name="getEvents"></a>
# **getEvents**
> [SubscribedEvent] getEvents(opts)

Returns all events for this client since the last call

Need scope of events/poll

### Example
```javascript
import {GdxDataShareApi} from 'gdx_data_share_api';

let apiInstance = new EventPlatformApi();
let opts = { 
  'eventType': "eventType_example", // String | Event Types required, if none supplied it will be the allowed types for this client
  'fromTime': new Date("2013-10-20T19:20:30+01:00"), // Date | Events after this time, if not supplied it will be from the last time this endpoint was called for this client
  'toTime': new Date("2013-10-20T19:20:30+01:00") // Date | Events before this time, if not supplied it will be now
};
apiInstance.getEvents(opts, (error, data, response) => {
  if (error) {
    console.error(error);
  } else {
    console.log('API called successfully. Returned data: ' + data);
  }
});
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **eventType** | **String**| Event Types required, if none supplied it will be the allowed types for this client | [optional] 
 **fromTime** | [**Date**](.md)| Events after this time, if not supplied it will be from the last time this endpoint was called for this client | [optional] 
 **toTime** | [**Date**](.md)| Events before this time, if not supplied it will be now | [optional] 

### Return type

[**[SubscribedEvent]**](SubscribedEvent.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

