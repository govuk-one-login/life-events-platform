package uk.gov.di.data.lep.dto;

import uk.gov.di.data.lep.library.dto.GroAddressStructure;
import uk.gov.di.data.lep.library.dto.GroPersonNameStructure;
import uk.gov.di.data.lep.library.dto.PersonBirthDateStructure;
import uk.gov.di.data.lep.library.dto.PersonDeathDateStructure;
import uk.gov.di.data.lep.library.enums.GenderAtRegistration;
import uk.gov.di.data.lep.library.enums.GroVerificationLevel;

import java.time.LocalDate;
import java.util.List;

public class FieldOptions {
    private FieldOptions() {
        throw new IllegalStateException("Utility class");
    }

    public static final List<String> TITLE = List.of(
        "Mr",
        "Mrs",
        "Ms",
        "Mx",
        "Dr"
    );
    public static final List<List<String>> FORENAME = List.of(
        List.of("BARRY"),
        List.of("MARY", "ALICE"),
        List.of("CHRISTOF"),
        List.of("ALONZO"),
        List.of("JAMES", "MATTHEW", "TIMOTHY")
    );
    public static final List<String> SURNAME = List.of(
        "SMITH",
        "JONES",
        "PEREZ",
        "KIM",
        "LEE-EVANS"
    );
    public static final List<List<GroPersonNameStructure>> ALIAS_NAME = List.of(
        List.of(
            new GroPersonNameStructure("Ms", List.of("CLARISSA"), "DE BASTIANI", null, null)
        ),
        List.of(
            new GroPersonNameStructure("Mx", List.of("SAMUEL"), "WILLS", null, "SAM")
        ),
        List.of(
            new GroPersonNameStructure("Mrs", List.of("CHRISTINA"), "CHETWORTH", "OBE", null),
            new GroPersonNameStructure("Mr", List.of("CHRIS"), "CHETWORTH", null, null)
        ),
        List.of(
            new GroPersonNameStructure("Mr", List.of("JIM", "JACK"), "BARON", null, null)
        ),
        List.of(
            new GroPersonNameStructure("Dr", List.of("JANE"), "GROVES", "PhD", null),
            new GroPersonNameStructure("Mrs", List.of("JANE"), "GROVES", null, null),
            new GroPersonNameStructure("Ms", List.of("JANE"), "PARR", null, null),
            new GroPersonNameStructure("Ms", List.of("JANE", "AMY"), "PARR", null, null)
        )
    );
    public static final List<List<String>> ALIAS_NAME_TYPE = List.of(
        List.of("otherwise"),
        List.of("formerly known as")
    );
    public static final List<String> MAIDEN_NAME = List.of(
        "TAYLOR",
        "PATEL",
        "TURNER",
        "LOVELACE",
        "TAKAHASHI JOHNSON"
    );
    public static final List<PersonBirthDateStructure> DATE_OF_BIRTH = List.of(
        new PersonBirthDateStructure(LocalDate.parse("1912-02-29"), null),
        new PersonBirthDateStructure(LocalDate.parse("2012-07-31"), null),
        new PersonBirthDateStructure(LocalDate.parse("1998-10-13"), GroVerificationLevel.LEVEL_0),
        new PersonBirthDateStructure(LocalDate.parse("1975-01-01"), GroVerificationLevel.LEVEL_3),
        new PersonBirthDateStructure(LocalDate.parse("1996-12-23"), GroVerificationLevel.LEVEL_2)
    );
    public static final List<PartialDateStructure> PARTIAL_DATE_OF_BIRTH = List.of(
        new PartialDateStructure(4, 1987, "Free format birth date", null),
        new PartialDateStructure(null, 1965, "", null),
        new PartialDateStructure(6, 2007, null, null),
        new PartialDateStructure(9, 2021, null, null),
        new PartialDateStructure(12, 2019, "freeformatbirthdate", null)
    );
    public static final List<PersonDeathDateStructure> DATE_OF_DEATH = List.of(
        new PersonDeathDateStructure(LocalDate.parse("2009-01-11"), null),
        new PersonDeathDateStructure(LocalDate.parse("2000-08-28"), GroVerificationLevel.LEVEL_1),
        new PersonDeathDateStructure(LocalDate.parse("1967-09-06"), null),
        new PersonDeathDateStructure(LocalDate.parse("1988-12-25"), GroVerificationLevel.LEVEL_2),
        new PersonDeathDateStructure(LocalDate.parse("1943-11-19"), null)
    );
    public static final List<PartialDateStructure> PARTIAL_DATE_OF_DEATH = List.of(
        new PartialDateStructure(10, 1998, "Free format death date", "Qualifier text"),
        new PartialDateStructure(null, 1958, "", ""),
        new PartialDateStructure(2, 1989, null, null),
        new PartialDateStructure(4, 2016, null, null),
        new PartialDateStructure(5, 2002, "freeformatdeathdate", "qualifiertext")
    );
    public static final List<GenderAtRegistration> GENDER = List.of(
        GenderAtRegistration.MALE,
        GenderAtRegistration.FEMALE,
        GenderAtRegistration.INDETERMINATE
    );
    public static final List<GroAddressStructure> ADDRESS = List.of(
        new GroAddressStructure(null, null, List.of("10 Test Street", "Test Town", "Test County"), "PR8 1HY"),
        new GroAddressStructure("16", "Ancaster House", List.of("Westminster", "London"), "G60 5EP"),
        new GroAddressStructure(null, null, List.of("27 Brown Lane", "Somerset"), "IV44 8RF"),
        new GroAddressStructure(null, null, List.of("65 Link Road", "Derbyshire"), "BR2 7JG"),
        new GroAddressStructure(null, null, List.of("Springfield", "Telham", "Kent"), "NR99 1AB")
    );
}
