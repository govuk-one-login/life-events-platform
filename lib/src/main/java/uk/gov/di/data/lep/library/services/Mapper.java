package uk.gov.di.data.lep.library.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Mapper {
    private Mapper() {
        throw new IllegalStateException("Utility class");
    }

    public static ObjectMapper objectMapper() {
        var mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        mapper.registerModule(new JavaTimeModule());
        mapper.setFilterProvider(
            new SimpleFilterProvider().addFilter("DeathNotificationSet", SimpleBeanPropertyFilter.serializeAll())
        );
        return mapper;
    }

    public static XmlMapper xmlMapper() {
        return XmlMapper.builder()
            .addModule(new JavaTimeModule())
            .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
            .defaultUseWrapper(false)
            .build();
    }
}
