package uk.gov.di.data.lep.library.dto;

import uk.gov.di.data.lep.library.enums.GenderAtRegistration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class GroJsonRecordBuilder {
    private LocalDateTime recordLockedDateTime;
    private LocalDateTime recordUpdateDateTime;
    private Integer recordUpdateReason;
    private GroPersonNameStructure deceasedName;
    private List<GroPersonNameStructure> deceasedAliasNames;
    private List<String> deceasedAliasNameTypes;
    private String deceasedMaidenName;
    private PersonDeathDateStructure deceasedDeathDate;
    private Integer partialMonthOfDeath;
    private Integer partialYearOfDeath;
    private PersonBirthDateStructure deceasedBirthDate;
    private Integer partialMonthOfBirth;
    private Integer partialYearOfBirth;

    public GroJsonRecordBuilder() {
        recordLockedDateTime = LocalDateTime.parse("2023-03-06T09:30:50");
        deceasedName = new GroPersonNameStructure("Mrs", List.of("ERICA"), "BLOGG", null, null);
        deceasedDeathDate = new PersonDeathDateStructure(LocalDate.parse("2007-03-06"), null);
        deceasedBirthDate = new PersonBirthDateStructure(LocalDate.parse("1967-03-06"), null);
    }

    public GroJsonRecordBuilder withLockedDateTime(LocalDateTime recordLockedDateTime) {
        this.recordLockedDateTime = recordLockedDateTime;
        return this;
    }

    public GroJsonRecordBuilder withUpdateDateTime(LocalDateTime recordUpdateDateTime) {
        this.recordUpdateDateTime = recordUpdateDateTime;
        return this;
    }

    public GroJsonRecordBuilder withUpdateReason(Integer recordUpdateReason) {
        this.recordUpdateReason = recordUpdateReason;
        return this;
    }

    public GroJsonRecordBuilder withName(GroPersonNameStructure name) {
        this.deceasedName = name;
        return this;
    }

    public GroJsonRecordBuilder withAliases(List<GroPersonNameStructure> names, List<String> types) {
        this.deceasedAliasNames = names;
        this.deceasedAliasNameTypes = types;
        return this;
    }

    public GroJsonRecordBuilder withMaidenName(String maidenName) {
        this.deceasedMaidenName = maidenName;
        return this;
    }

    public GroJsonRecordBuilder withDeathDate(LocalDate deathDate) {
        if (deathDate == null) {
            deceasedDeathDate = null;
        } else {
            deceasedDeathDate = new PersonDeathDateStructure(deathDate, null);
        }
        return this;
    }

    public GroJsonRecordBuilder withDeathYear(Integer partialYearOfDeath) {
        this.partialYearOfDeath = partialYearOfDeath;
        return this;
    }

    public GroJsonRecordBuilder withDeathMonth(Integer partialMonthOfDeath) {
        this.partialMonthOfDeath = partialMonthOfDeath;
        return this;
    }

    public GroJsonRecordBuilder withBirthDate(LocalDate birthDate) {
        if (birthDate == null) {
            deceasedBirthDate = null;
        } else {
            deceasedBirthDate = new PersonBirthDateStructure(birthDate, null);
        }
        return this;
    }

    public GroJsonRecordBuilder withBirthYear(Integer partialYearOfBirth) {
        this.partialYearOfBirth = partialYearOfBirth;
        return this;
    }

    public GroJsonRecordBuilder withBirthMonth(Integer partialMonthOfBirth) {
        this.partialMonthOfBirth = partialMonthOfBirth;
        return this;
    }

    public GroJsonRecord build() {
        return new GroJsonRecord(
            1234567890,
            1,
            recordLockedDateTime,
            recordUpdateDateTime,
            recordUpdateReason,
            deceasedName,
            deceasedAliasNames,
            deceasedAliasNameTypes,
            deceasedMaidenName,
            GenderAtRegistration.FEMALE,
            deceasedDeathDate,
            partialMonthOfDeath,
            partialYearOfDeath,
            null,
            null,
            deceasedBirthDate,
            partialMonthOfBirth,
            partialYearOfBirth,
            null,
            new GroAddressStructure(null, null, List.of("123 Street"), "GT8 5HG")
        );
    }
}
