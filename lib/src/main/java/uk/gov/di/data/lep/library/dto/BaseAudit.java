package uk.gov.di.data.lep.library.dto;

import java.time.Instant;

public interface BaseAudit {
    default String componentID() {
        return "LEP";
    }
    default Long timestamp() {
        return Instant.now().getEpochSecond();
    }
    String eventName();
}
