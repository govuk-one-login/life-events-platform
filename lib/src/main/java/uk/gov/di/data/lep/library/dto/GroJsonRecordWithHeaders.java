package uk.gov.di.data.lep.library.dto;

import uk.gov.di.data.lep.library.dto.gro.GroJsonRecord;

public record GroJsonRecordWithHeaders(
    GroJsonRecord groJsonRecord,
    String authenticationToken,

    String correlationId
) {
}
