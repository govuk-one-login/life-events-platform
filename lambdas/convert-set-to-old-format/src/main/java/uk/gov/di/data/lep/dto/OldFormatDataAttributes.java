package uk.gov.di.data.lep.dto;

import uk.gov.di.data.lep.library.enums.EventType;

public record OldFormatDataAttributes(
    EventType eventType,
    String sourceId,
    OldFormatEventData eventData,   //TODO: either this of this
    Boolean dataIncluded    //TODO: either this or this
) {
}
