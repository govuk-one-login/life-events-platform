package uk.gov.di.data.lep.library.dto.deathnotification.audit;

import uk.gov.di.data.lep.library.dto.BaseAudit;

public record GroPublishRecordAudit(
    String eventName,
    GroPublishRecordAuditExtensions extensions
) implements BaseAudit {
    public GroPublishRecordAudit(GroPublishRecordAuditExtensions extensions) {
        this("GRO_PUBLISH_RECORD", extensions);
    }
}
