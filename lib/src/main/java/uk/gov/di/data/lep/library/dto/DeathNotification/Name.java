package uk.gov.di.data.lep.library.dto.DeathNotification;

import java.util.List;

public record Name(
    String description,
    List<NamePart> nameParts,
    String validFrom,
    String validUntil
) {
}
