package uk.gov.di.data.lep.library.dto.gro.audit;

public record GroConvertToJsonAuditExtensions(
    String correlationID,
    Integer fileHash
) {
}
