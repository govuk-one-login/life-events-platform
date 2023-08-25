package uk.gov.di.data.lep.library.dto.gro;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import java.util.List;

public record GroAddressStructure(
    @JsonProperty("Flat")
    String flat,
    @JsonProperty("Building")
    String building,
    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("Line")
    List<String> lines,
    @JsonProperty("Postcode")
    String postcode
) {
}
