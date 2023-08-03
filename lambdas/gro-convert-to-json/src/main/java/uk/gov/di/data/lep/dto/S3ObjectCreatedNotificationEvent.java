package uk.gov.di.data.lep.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record S3ObjectCreatedNotificationEvent(
    String version,
    String id,
    @JsonProperty("detail-type")
    String detailType,
    String source,
    String account,
    String time,
    String region,
    List<String> resources,
    S3ObjectCreatedNotificationEventDetail detail
) {
}
