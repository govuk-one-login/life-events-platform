package uk.gov.di.data.lep.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class S3ObjectCreatedNotificationEvent {
    public String version;
    public String id;
    @JsonProperty("detail-type")
    public String detailType;
    public String source;
    public String account;
    public String time;
    public String region;
    public List<String> resources;
    public S3ObjectCreatedNotificationEventDetail detail;
}
