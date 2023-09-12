package uk.gov.di.data.lep.library.dto.deathnotification;

import com.fasterxml.jackson.annotation.JsonFilter;

import java.util.List;

@JsonFilter("DeathNotificationSet")
public record DeathRegistrationSubject (
    List<PostalAddress> address,
    List<DateWithDescription> birthDate,
    List<Name> name,
    List<Sex> sex,
    String freeFormatBirthDate
){
}
