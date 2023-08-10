package uk.gov.di.data.lep.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import uk.gov.di.data.lep.library.dto.GroJsonRecord;

import java.util.List;

public class DeathRegistrationGroup {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("DeathRegistration")
    public List<GroJsonRecord> deathRegistrations;
    @JsonProperty("RecordCount")
    public int recordCount;
}
