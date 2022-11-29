# EventToPublish

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**eventType** | **String** | Type of event | 
**eventTime** | **String** | Date and time when the event took place, default is now | [optional] 
**id** | **String** | ID that references the event (optional) | [optional] 
**eventDetails** | **String** | Json payload of data, normally no additional data would be sent | [optional] 

<a name="EventTypeEnum"></a>
## Enum: EventTypeEnum

* `DEATH_NOTIFICATION` (value: `"DEATH_NOTIFICATION"`)
* `BIRTH_NOTIFICATION` (value: `"BIRTH_NOTIFICATION"`)
* `MARRIAGE_NOTIFICATION` (value: `"MARRIAGE_NOTIFICATION"`)

