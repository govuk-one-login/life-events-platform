package uk.gov.di.data.lep.library.dto;

import uk.gov.di.data.lep.library.dto.gro.GroJsonRecord;

public record GroJsonRecordWithCorrelationId(
    GroJsonRecord groJsonRecord,
    String correlationId
) {
}
