package uk.gov.di.data.lep.library.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import java.util.List;

public record GroPersonNameStructure(
    @JsonAlias("PersonNameTitle")
    String personNameTitle,
    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonAlias("PersonGivenName")
    List<String> personGivenNames,
    @JsonAlias("PersonFamilyName")
    String personFamilyName,
    @JsonAlias("PersonNameSuffix")
    String personNameSuffix,
    @JsonAlias("PersonRequestedName")
    String personRequestedName
) {
}
