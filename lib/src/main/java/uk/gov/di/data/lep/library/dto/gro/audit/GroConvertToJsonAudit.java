package uk.gov.di.data.lep.library.dto.gro.audit;

import uk.gov.di.data.lep.library.dto.BaseAudit;

public record GroConvertToJsonAudit(
    String eventName,
    GroConvertToJsonAuditExtensions extensions
) implements BaseAudit {
    public GroConvertToJsonAudit(GroConvertToJsonAuditExtensions extensions) {
        this("GRO_CONVERT_TO_JSON", extensions);
    }
}
