package uk.gov.di.data.lep.library.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.temporal.TemporalAccessor;

public class Mapper {
    private Mapper() {
        throw new IllegalStateException("Utility class");
    }

    public static ObjectMapper objectMapper() {
        var mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    public static XmlMapper xmlMapper() {
        var mapper = new XmlMapper();
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
