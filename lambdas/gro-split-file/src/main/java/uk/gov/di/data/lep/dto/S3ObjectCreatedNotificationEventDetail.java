package uk.gov.di.data.lep.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class S3ObjectCreatedNotificationEventDetail {
    public String version;
    public S3ObjectCreatedNotificationEventBucket bucket;
    public S3ObjectCreatedNotificationEventObject object;
    @JsonProperty("request-id")
    public String requestId;
    public String requester;
    @JsonProperty("source-ip-address")
    public String sourceIpAddress;
    public String reason;
}
