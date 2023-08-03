package uk.gov.di.data.lep.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record S3ObjectCreatedNotificationEventDetail(
    String version,
    S3ObjectCreatedNotificationEventBucket bucket,
    S3ObjectCreatedNotificationEventObject object,
    @JsonProperty("request-id")
    String requestId,
    String requester,
    @JsonProperty("source-ip-address")
    String sourceIpAddress,
    String reason
) {
}
