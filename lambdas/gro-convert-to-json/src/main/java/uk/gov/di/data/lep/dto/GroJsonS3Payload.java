package uk.gov.di.data.lep.dto;

import uk.gov.di.data.lep.library.dto.GroJsonRecord;

import java.util.List;

public record GroJsonS3Payload (
    List<GroJsonRecord> payload,
    String bucket,
    String key
){}
