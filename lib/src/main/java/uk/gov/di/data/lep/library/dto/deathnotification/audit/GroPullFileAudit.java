package uk.gov.di.data.lep.library.dto.deathnotification.audit;

import uk.gov.di.data.lep.library.dto.BaseAudit;

public record GroPullFileAudit(
    String eventName,
    GroPullFileAuditExtensions extensions
) implements BaseAudit {
    public GroPullFileAudit(GroPullFileAuditExtensions extensions) {
        this("GRO_PULL_FILE", extensions);
    }
}
