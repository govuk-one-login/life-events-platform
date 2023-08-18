package uk.gov.di.data.lep.library.dto.deathnotification;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public record NamePart(
    NamePartType type,
    String value,
    LocalDate validFrom,
    LocalDate validUntil
) {
    public NamePart(NamePartType type, String value) {
        this(type, value, null, null);
    }
}
