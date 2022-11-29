# GdxDataShareApi.EventListenerApi

All URIs are relative to *http://localhost:8080*

Method | HTTP request | Description
------------- | ------------- | -------------
[**publishEvent**](EventListenerApi.md#publishEvent) | **POST** /event-data-receiver | Send events to GDS - Source

<a name="publishEvent"></a>
# **publishEvent**
> publishEvent(body)

Send events to GDS - Source

Scope is data_receiver/notify

### Example
```javascript
import {GdxDataShareApi} from 'gdx_data_share_api';

let apiInstance = new GdxDataShareApi.EventListenerApi();
let body = new GdxDataShareApi.EventToPublish(); // EventToPublish | 

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

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined

