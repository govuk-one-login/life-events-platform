package uk.gov.di.data.lep.dto;

import uk.gov.di.data.lep.library.dto.GroDeathEventDetails;
import uk.gov.di.data.lep.library.enums.EnrichmentField;
import uk.gov.di.data.lep.library.enums.EventType;

import java.util.List;
import java.util.UUID;

public record GroDeathEventNotification (
    UUID eventId,
    EventType eventType,
    String sourceId,
    List<EnrichmentField> enrichmentFields,
    GroDeathEventDetails eventDetails
){}
