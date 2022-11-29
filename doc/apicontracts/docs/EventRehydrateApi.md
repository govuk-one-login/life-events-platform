# EventRehydrateApi

All URIs are relative to *http://localhost:8080*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getEventDetails**](EventRehydrateApi.md#getEventDetails) | **GET** /event-data-retrieval/{id} | Event Rehydrate API - Lookup event data

<a name="getEventDetails"></a>
# **getEventDetails**
> EventInformation getEventDetails(id)

Event Rehydrate API - Lookup event data

The event ID is the UUID received off the queue, Need scope of data_retriever/read

### Example
```javascript
import {GdxDataShareApi} from 'gdx_data_share_api';

let apiInstance = new EventRehydrateApi();
let id = "id_example"; // String | Event ID

apiInstance.getEventDetails(id, (error, data, response) => {
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
 **id** | [**String**](.md)| Event ID | 

### Return type

[**EventInformation**](EventInformation.md)

### Authorization

OAUTH2 client credentials.

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

