package uk.gov.di.data.lep.library.dto.deathnotification;

import java.time.LocalDate;

public record PostalAddress(
    String addressCountry,
    String addressLocality,
    String buildingName,
    String buildingNumber,
    String departmentName,
    String dependentAddressLocality,
    String dependentStreetName,
    String doubleDependentAddressLocality,
    String organisationName,
    String postalCode,
    String streetName,
    String subBuildingName,
    Integer uprn,
    LocalDate validFrom,
    LocalDate validUntil
) {
}
