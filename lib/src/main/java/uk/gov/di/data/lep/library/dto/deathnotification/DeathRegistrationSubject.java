package uk.gov.di.data.lep.library.dto.deathnotification;

import java.util.List;

public record DeathRegistrationSubject (
    List<PostalAddress> address,
    List<IsoDate> birthDate,
    List<Name> name,
    List<Sex> sex
){
}
