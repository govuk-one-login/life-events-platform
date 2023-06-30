package uk.gov.di.data.lep.classes;

import uk.gov.di.data.lep.enums.GroSex;

import java.time.LocalDate;

// This is the input data - all fields will be populated
public class GroDeathEventEnrichedData {
    public String sourceId;
    public GroSex sex;
    public LocalDate dateOfBirth;
    public LocalDate dateOfDeath;
    public String registrationId;
    public LocalDate eventTime;
    public String verificationLevel;
    public String partialMonthOfDeath;
    public String partialYearOfDeath;
    public String forenames;
    public String surname;
    public String maidenSurname;
    public String addressLine1;
    public String addressLine2;
    public String addressLine3;
    public String addressLine4;
    public String postcode;
    public GroDeathEventEnrichedData(
            String sourceId,
            GroSex sex,
            LocalDate dateOfBirth,
            LocalDate dateOfDeath,
            String registrationId,
            LocalDate eventTime,
            String verificationLevel,
            String partialMonthOfDeath,
            String partialYearOfDeath,
            String forenames,
            String surname,
            String maidenSurname,
            String addressLine1,
            String addressLine2,
            String addressLine3,
            String addressLine4,
            String postcode) {
        this.sourceId = sourceId;
        this.sex = sex;
        this.dateOfBirth = dateOfBirth;
        this.dateOfDeath = dateOfDeath;
        this.registrationId = registrationId;
        this.eventTime = eventTime;
        this.verificationLevel = verificationLevel;
        this.partialMonthOfDeath = partialMonthOfDeath;
        this.partialYearOfDeath = partialYearOfDeath;
        this.forenames = forenames;
        this.surname = surname;
        this.maidenSurname = maidenSurname;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.addressLine3 = addressLine3;
        this.addressLine4 = addressLine4;
        this.postcode = postcode;
    }
}
