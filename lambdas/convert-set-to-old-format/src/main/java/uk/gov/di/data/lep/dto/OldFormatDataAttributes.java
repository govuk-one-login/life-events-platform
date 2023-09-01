package uk.gov.di.data.lep.dto;

import uk.gov.di.data.lep.library.enums.EventType;

public record OldFormatDataAttributes(
    EventType eventType,
    String sourceId,
    OldFormatEventData eventData,
    Boolean dataIncluded
) {
}
