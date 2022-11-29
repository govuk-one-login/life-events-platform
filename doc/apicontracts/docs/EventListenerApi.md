# EventListenerApi

All URIs are relative to *http://localhost:8080*

Method | HTTP request | Description
------------- | ------------- | -------------
[**publishEvent**](EventListenerApi.md#publishEvent) | **POST** /event-data-receiver | Send events to GDS - The &#x27;Source&#x27; of the event - this could be HMPO or DWP for example

<a name="publishEvent"></a>
# **publishEvent**
> publishEvent(body)

Send events to GDS - The &#x27;Source&#x27; of the event - this could be HMPO or DWP for example

Scope is data_receiver/notify

### Example
```javascript
import {GdxDataShareApi} from 'gdx_data_share_api';

let apiInstance = new EventListenerApi();
let body = new EventToPublish(); // EventToPublish | 

apiInstance.publishEvent(body, (error, data, response) => {
  if (error) {
    console.error(error);
  } else {
    console.log('API called successfully.');
  }
});
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**EventToPublish**](EventToPublish.md)|  | 

### Return type

null (empty response body)

### Authorization

OAUTH2 client credentials.

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined

