package uk.gov.di.data.lep.dto;

import uk.gov.di.data.lep.library.dto.GroDeathEventDetails;
import uk.gov.di.data.lep.library.enums.EnrichmentField;
import uk.gov.di.data.lep.library.enums.EventType;

import java.util.List;
import java.util.UUID;

public class GroDeathEventNotification {
    public UUID eventId;
    public EventType eventType;
    public String sourceId;
    public List<EnrichmentField> enrichmentFields;
    public GroDeathEventDetails eventDetails;
}
