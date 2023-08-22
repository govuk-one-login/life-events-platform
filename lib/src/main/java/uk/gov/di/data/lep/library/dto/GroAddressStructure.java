package uk.gov.di.data.lep.library.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import java.util.List;

public record GroAddressStructure(
    @JsonAlias("Flat")
    String flat,
    @JsonAlias("Building")
    String building,
    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonAlias("Line")
    List<String> lines,
    @JsonAlias("Postcode")
    String postcode
) {
}
