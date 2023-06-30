package uk.gov.di.data.lep.classes;

import uk.gov.di.data.lep.enums.EnrichmentField;
import uk.gov.di.data.lep.enums.EventType;

import java.util.List;
import java.util.UUID;

public class GroDeathEventNotification {
    public UUID eventId;
    public EventType eventType;
    public String sourceId; // Optional
    public Boolean dataIncluded;
    public List<EnrichmentField> enrichmentFields;
    public GroDeathEventDetails eventDetails; // Optional

};
