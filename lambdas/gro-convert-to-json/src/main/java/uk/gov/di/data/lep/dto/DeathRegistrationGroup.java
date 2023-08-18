package uk.gov.di.data.lep.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import uk.gov.di.data.lep.library.dto.GroJsonRecord;

import java.util.List;

public record DeathRegistrationGroup(
    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("DeathRegistration")
     List<GroJsonRecord> deathRegistrations,
    @JsonProperty("RecordCount")
     int recordCount
){
}

