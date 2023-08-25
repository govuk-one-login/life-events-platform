package uk.gov.di.data.lep.library.dto;

public record GroJsonRecordWithAuth (
    GroJsonRecord groJsonRecord,
    String authenticationToken
){}
