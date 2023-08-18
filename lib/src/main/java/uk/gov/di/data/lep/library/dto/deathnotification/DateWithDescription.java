package uk.gov.di.data.lep.library.dto.deathnotification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.gov.di.data.lep.library.services.PartialDateDeserialiser;
import uk.gov.di.data.lep.library.services.PartialDateSerialiser;

import java.time.temporal.TemporalAccessor;

public record DateWithDescription(
    String description,
    @JsonDeserialize(using = PartialDateDeserialiser.class)
    @JsonSerialize(using = PartialDateSerialiser.class)
    TemporalAccessor value
) {
}
