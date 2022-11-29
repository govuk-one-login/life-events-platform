# GdxDataShareApi.EventPlatformApi

All URIs are relative to *http://localhost:8080*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getEvents**](EventPlatformApi.md#getEvents) | **GET** /events | Tell me about events

<a name="getEvents"></a>
# **getEvents**
> [SubscribedEvent] getEvents(opts)

Tell me about events

Need scope of events/poll

### Example
```javascript
import {GdxDataShareApi} from 'gdx_data_share_api';

let apiInstance = new GdxDataShareApi.EventPlatformApi();
let opts = { 
  'eventType': "eventType_example", // String | Event Types
  'fromTime': new Date("2013-10-20T19:20:30+01:00"), // Date | Events after this time
  'toTime': new Date("2013-10-20T19:20:30+01:00") // Date | Events before this time
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
 **eventType** | **String**| Event Types | [optional] 
 **fromTime** | [**Date**](.md)| Events after this time | [optional] 
 **toTime** | [**Date**](.md)| Events before this time | [optional] 

### Return type

[**[SubscribedEvent]**](SubscribedEvent.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

